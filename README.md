# Gotcha

## Overview
Gotcha is a python2 program that parse a source code to extract developers comments. These comments are written in a special format called Gotcha Keywords, and they are used to point in a formal way bugs in the code, parts of code that may be rewritten, documentation… Any kind of information useful for developpers.

These gotcha keywords are very useful during the developping phase and they allow developers to track bugs and see the amount of remaining work in a blink of an eye. In fact, when we use Gotcha to display, for example, "TODO" comments, we can see the tasks we have to achieve to progress in the project. A search on the keyword "BUG" lists parts of the code that we have to debug and "TRICKY" comments are use to explain a part of code that might be hard to understand by only reading it.

## Comments format
A comment Gotcha have to be written like that :
        CS :KEYWORD:dev-name:yyyymmdd:Comment

"CS" stands for Comment Symbol. The keyword heve to be written uppercase. The date have to respect the format yyyymmdd.

Example with a C comment :
        // :TODO:Meow:20121221:Find a way to survive to the end of the world

Exemple with a shellscript comment :
        # :BUG:Meow:20120405:This function just don't work at all...

## Keywords
Here is the list of the Gotcha Keywords that Gotcha is able to parse, and their sense:

* BUG : Describes a bug in the code, with a brief description next line if possible 
* DEBUG : Indicate that a developer is working on the bug described before this comment 
* WARNING : Important information for developers, for example, to specify that a method is working but might be unsafe… 
* TODO : Describes a task to do 
* KLUDGE : Describes a piece of code that have to be rewritten before release (quick and dirty code) 
* TRICKY : Used to explain some technical code, hard to understand by only reading it. 
* DOC : Description of the code for documentation 
* COMMENT : A "classic" comment, that might stay after the release 

These keywords are sorted in classes, here are the definitions:

* Critical : Describes critical information, work that have to be done as soon as possible. 
* Important : Important informations, some tasks to do 
* Info : Informative comments that must stay in documentation 
* Notice : Notes, comments, … 

## How to use Gotcha
Just run gotcha. With -h to display all options available.

Gotcha can display all gotcha keywords but also and moreover, information you want. We can specify files to parse, to ignore, tell him to parse every file in every sub-directory, on only in the current one. It is possible to filter results by keywords class, ignore some keywords, just display keywords you want to see. Finally we can also sort results by keyword, by developer name and by date. We can display information that suits our own needs.

For more information I suggest you to start the program with the -h option. You will see all the options and some examples of use.

## Install
You can install Gotcha with pip : pip install gotcha

## Sources
Sources are freely available on github : http://github.com/meowstars/gotcha

## License
Gotcha is published under the LGPL version 3 license.

