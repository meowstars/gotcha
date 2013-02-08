#!/usr/bin/python2
#-*- coding:utf-8 -*-
import os
import re
import sys
import codecs
import clize
from termcolor import colored
from clint.textui import indent, puts

# Keywords managed
# Add others if needed
keywords = ["BUG", "DEBUG", "TODO", "TRICKY", "KLUDGE", "WARNING", "COMMENT",
            "DOC"]
keywords = sorted(keywords)

# Keywords are sorting into classes
# These classes are :
#   Critical = points that have to be fixed asap
#   Important = points that have to be done, of rework
#   Notice = Comments for developers in code
#   Info = Things that have to be documented deeper
# Order them by importance
classes = ["critical", "important", "notice", "info"]

# Keywords mapping
# Colors are set for each classes with termcolor
# Feel free to change them
kwclasses = {
    'critical': [lambda x: colored(x, 'grey', 'on_red'),
                 ['BUG', 'DEBUG', 'WARNING']],
    'important': [lambda x: colored(x, 'grey', 'on_yellow'),
                  ['TODO', 'KLUDGE']],
    'notice': [lambda x: colored(x, 'grey', 'on_green'),
               ['COMMENT']],
    'info': [lambda x: colored(x, 'grey', 'on_blue'),
             ['TRICKY', 'DOC']]
}

# Globals
options = set()
ignorefiles = set()
exp_keywords = dict()
gotchas = dict()


def make_pattern():
    """
    Build the search pattern from keywords
    """
    # Format: comment-char :KEYWORD:DevName:yymmdd:Comment
    # Anything before gotcha kw
    str_pattern = r"^(.)*:("
    isFirst = True
    for kw in keywords:
        # Add each keyword in the pattern
        if not isFirst:
            str_pattern += r"|("
        else:
            isFirst = False
            str_pattern += "("
        str_pattern += kw
        str_pattern += ")"
    # Add the remaining part of the gotcha kw
    str_pattern += r"):[A-Za-z0-9_-]*:"
    str_pattern += r"[0-9]{2}((0[0-9])|(1[0-2]))(([0-2][0-9])|(3[0-1])):(.)*$"
    return re.compile(str_pattern)


def display_report():
    """
    Display a report of the gotchas keywords parsed
    It gives each wanted keyword and count
    """
    text = ''
    for kwclass in classes:
        color = kwclasses[kwclass][0]
        kws = kwclasses[kwclass][1]
        for kw in kws:
            if kw not in keywords:
                continue
            text = text + color(kw) + ': ' + str(len(gotchas[kw])) + ' | '
    print text


def display_gotchas(wanted_classes):
    """
    Display each gotcha parsed sorted by keywords
    """
    for kwclass in wanted_classes:
        # Set color and keywords for each class
        color = kwclasses[kwclass][0]
        kws = kwclasses[kwclass][1]
        print color(kwclass)
        with indent(3):
            for kw in kws:
                if kw not in keywords:
                    continue
                kw_gotcha = gotchas[kw]
                if len(kw_gotcha) > 0:
                    for gotcha in kw_gotcha:
                        puts(gotcha[0] + ' line ' + str(gotcha[1]) + ': ' +
                             color(kw) + ' ' + ' '.join(gotcha[2]))


# Gotcha dicts are designed like that:
# {'keyword', [file, line, [devname, date, comment]], ...}
def parse(path):
    """
    Parse current file in path
    """
    filename = path.split('/')[-1]
    if 'ignore' in options and filename in ignorefiles:
        if 'verbose' in options:
            print 'File {0} ignored'.format(filename)
        return
    openfile = codecs.open(path, 'r', encoding='utf8')
    content = openfile.read()
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if exp_keywords['pattern'].search(line):
            for kw in keywords:
                if exp_keywords[kw].search(line):
                    gotchas[kw].append([path, i + 1,
                                        line.split(kw).pop().split(':')[1:]])
                    break


def explore_path(path):
    """
    Function that explores the path given as argument
    """
    if os.path.isfile(path):
        parse(path)
    else:
        try:
            if 'verbose' in options:
                print 'Exploring {0}'.format(path)
            for filename in os.listdir(path):
                # ignore hidden and temp files
                if filename[0] == '.' or filename[-1] == '~':
                    continue
                f = path + '/' + filename
                if os.path.isdir(f) and 'recursive' in options:
                    explore_path(f)
                elif os.path.isfile(f):
                    parse(f)
        except OSError as error:
            print error
            sys.exit(2)


def do_filter(string):
    """
    Filter the gotchas with args
    returns the tuple (wanted classes, wanted keywords)
    """
    # Pattern 
    # -kKW : ignore keyword KW
    # +cCLASS : only class CLASS
    # +cCLASS,-kKW : Only class CLASS, ignore KW
    filter_exp = re.compile(r'^(?P<mode>[-+])(?P<type>[ck])(?P<catch>\w+)$')
    args = string.split(',')
    filter = {
        'c': {
            '-': [],
            '+': []
        },
        'k': {
            '-': [],
            '+': []
        }
    }
    for arg in args:
        match = filter_exp.search(arg)
        if match:
            filter[match.group('type')][match.group('mode')].append(match.group('catch').upper())
        else:
            print '{0} is not a valid filter'.format(arg)
    unwantedclass = []
    unwantedkw = []
    filterclass = filter['c']
    if len(filterclass['+']) != 0:
        #discard '-' filters
        for kwc in classes:
            if kwc.upper() not in filterclass['+']:
                unwantedclass.append(kwc)
    else:
        for kwc in classes:
            if kwc.upper() in filterclass['-']:
                unwantedclass.append(kwc)
    filterkw = filter['k']
    if len(filterkw['+']) != 0:
        #discard '-' filters
        for kw in keywords:
            if kw not in filterkw['+']:
                unwantedkw.append(kw)
    else:
        for kw in keywords:
            if kw in filterkw['-']:
                unwantedkw.append(kw)
    for kwc in unwantedclass:
        classes.remove(kwc)
    for kw in unwantedkw:
        keywords.remove(kw)


@clize.clize(alias={
    'recursive': ('r',),
    'filter': ('f',),
    'verbose': ('v',),
    'only_report': ('or',),
})
def gotcha_main(recursive=False, only_report=False, filter='', verbose=False, *paths):
    """
    A small parser of gotcha keywords.
    Keywords are like // :KEYWORD:Dev-name:yymmdd:Comment

    paths: paths where gotcha have to search keywords. If nothing is given
    gotcha just parse the current directory

    recursive: Tells gotcha if it has to search into subdirectories

    filter: filters by keywords or classes

    only_report: Displays only the report

    verbose: print more information
    
    Additionnal information about filter:
    If a + filter is given, - filter is discarded. 
    So if you do '+cimportant,-cwhatever', only the + one will be kept.
    More important, if KW is in a class C and you do '-cC,+kKW', your +kKW will
    not be accepted because you removed the entire class C before.
    """
    if verbose:
        options.add('verbose')
    if recursive:
        options.add('recursive')
    if only_report:
        options.add('only-report')
    if filter is not "":
        do_filter(filter)
    for kw in keywords:
        gotchas[kw] = []
        exp_keywords[kw] = re.compile(r"^(.)*:" + kw + r":(.)*$")
    exp_keywords['pattern'] = make_pattern()
    if paths:
        for path in paths:
            explore_path(path)
    else:
        explore_path('.')
    if not 'only-report' in options:
        display_gotchas(classes)
    display_report()


def gotcha():
    try:
        gotcha_main(*sys.argv)
    except clize.ArgumentError as e:
        print os.path.basename(sys.argv[0]) + ': ' + str(e)

if __name__ == '__main__':
    gotcha()
