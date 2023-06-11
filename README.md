# ATOM

**ATOM** is a modern programming language that not only embraces the art of concise code but also can be used in the realm of everyday programming, including scripting, servers, web applications, data mining, and more.  It takes influences from a wide variety of successful languages including Javascript, SQL, Perl, and of course, APL.  ATOM, above all, strives to serve the niche of Mobile Application Development.

To optimize the Mobile Application Development experience, ATOM adheres to specific design principles that address the unique characteristics of mobile devices and how end users interact with them. Some key considerations include:

 - Device screen size: Mobile screens are relatively small, making it challenging to display large amounts of information.

 - Window switching: Unlike computer users who can easily switch between windows with Alt+Tab, mobile users face a more cumbersome process.

 - Keyboard differences: Mobile applications benefit from international and emoji keyboards, offering users greater flexibility in typing compared to traditional computer keyboards.

ATOM tackles these challenges through deliberate design choices. It employs conciseness to ensure more code can fit on the screen, rethinks how functions operate to reduce the need for developers to constantly switch between documentation pages, and supports emojis, enabling engineers to leverage the full potential of modern mobile keyboards.

As the first ever language specifically designed to allow engineers to comfortably write code from their phones, ATOM seeks to bring about a paradigm shift centered on Mobile Application Development.

## Usage

The easiest way to run ATOM code is through the web interpreter. Just go here, type your code, and run it! There may be some bugs, it is still a work in progress.

[Online Interpreter - WORK IN PROGRESS](https://sanrensei.github.io/ATOM/)

To run it offline, ATOM mainly uses a java interpreter. Pull the code, open the java project, and add the following line of code anywhere in your project:

```
ATOMRuntime.processInput("1+1");
```

Feeling fancy and prefer to keep your program in a separate file? Fear not, for ATOM has you covered.

```
ATOMRuntime.processFile(new File("helloWorld.atom"));
```

## The Saga of ATOM

Once upon a time, an extraordinary software engineer roamed the lands. This engineer was a true embodiment of creativity, despising the notion of relying on other people's libraries. Naturally, there came a fateful day when a REST API's output needed parsing. JSON was the culprit. But instead of using Gson like any sane person would, our engineer embarked on a grand quest to forge their own JSON parser. Why, you ask? Simply because they could.

But the tale doesn't conclude there. This self-made JSON parser was then employed to decipher configuration files in a myriad of programs. However, something was amiss. Our protagonist loathed the absence of comments in JSON. They detested its inability to perform arithmetic operations. And oh, the agony of not being able to import sections of other JSONs! At this pivotal moment, a spark of unparalleled brilliance struck them. They dared to pose a question. What if JSON could achieve Turing completeness? What if JSON itself could metamorphose into a bona fide programming language?

Thus began the relentless transformation of the JSON parser into an abomination of unfathomable proportions. This unholy amalgamation boasted file imports, if statements, array mapping, string manipulation, and more. It was akin to Frankenstein's monster, a force to be reckoned with. Alas, it succumbed under the oppressive weight of its own existence, collapsing under the insurmountable burden of technical debt.

Years later, our resilient engineer, armed with newfound wisdom, decided to learn from their past missteps and set out to create a superior version from scratch. Something far beyond the confines of a mere fancy JSON parser. Behold, ATOM was born—a fully-fledged programming language that aspires to be the epitome of conciseness and perfection.

## Links

- [Assorted Code Samples](https://github.com/SanRenSei/ATOM/tree/main/samples/assorted)
- [Introduction to Syntax](https://github.com/SanRenSei/ATOM/blob/main/docs/Runtime.md)
- [Information on Operations](https://github.com/SanRenSei/ATOM/blob/main/docs/Operations.md)
- [Online Interpreter - WORK IN PROGRESS](https://sanrensei.github.io/ATOM/)