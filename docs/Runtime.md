# Introduction to ATOM Syntax

ATOM syntax is quite simple at a glance. There are three types of values that can exist. Primatives, Arrays, and Controllers. You can think of them as Atoms, Molecules, and Proteins if you prefer, but that would probably make it more confusing.

Now look here as I declare a few variables

```
a = 5;
b = "Hello World";
c = [1,2,3];
d = {"foo":"bar"}; 
```

If you're written javascript before, you'll probably feel right at home here. Everything does exactly what it looks like.

Now let's get into some of the first differences you might observe. There are several unary operators that exist within this language that offer basic functions that come standard with your average programming language.

Like this string trim from Java

```
"   A string with some spaces    ".trim()
```

In ATOM, you just need this unary operator

```
✂"   A string with some spaces    "
```

There are several unary operators for various data types, so visit the [Operations page](https://github.com/SanRenSei/ATOM/blob/main/docs/Operations.md) if you would like to know more about them.

The second major observation is that almost everything that does something is an operator. Think about foreach loops as an example. In a more primative language, you would have something like

```
[1,2,3,4,5].forEach(n => console.log(n))
```

Instead, in ATOM, foreach is an operator that looks like ∀. It takes an array and a controller (function), and does the rest from there.

```
[1,2,3,4,5] ∀ 🖨 $0
```

Let's take a closer look here. I said that the foreach operator takes an array on the left, and the left side looks like an array, so that's good, but what about the right side? ATOM has the concept of implicit scoping, where for certain operations that take a controller on the right, it will implicitly bundle it into a controller without having to put the braces around it.

$0 here is the parameter of the controller. In this case, it iterates between the values 1 to 5, and it gets printed. If you are inside some nested controllers, you can use $1, $2, $3, etc. to walk up the chain of parameters. Lastly, you can use * as a shorthand for $0. Therefore, this is just as valid and does the same thing.

```
[1,2,3,4,5] ∀ 🖨*
```

Watch out for some potential of confusion. Here is a snippet that finds the product of the numbers from 1 to 5.

```
product = 1;
1~5 ∀ product**;
```

Note that here, one of the * refers to the multiplication operation, and the second * refers to the parameter. You are allowed to do this and ATOM will correctly interpret the intention, but you may annoy your fellow engineers if they see you write this.

Let's wrap this up with a classic, FizzBuzz. Print out the numbers from 1 to 100, but if its a multiple of 3, print Fizz instead, and Buzz for 5, and FizzBuzz for 15. How can this be done with ATOM?

```
1~100 ∀ {
  *%15==0: 🖨"FizzBuzz",
  *%5==0: 🖨"Buzz",
  *%3==0: 🖨"Fizz",
  🖨*
}
```

It's that easy! The left side declares an array of values from 1 to 100, and a foreach gets called on it. The right side declares a controller that takes a variable. The different branches act as a switch statement, printing different outputs depending on whether the input variable is divisible by certain items.

With this, you should now be able to read any sort of ATOM code! If you see an operator that you don't recognize, go ahead and consult the  [Operations page](https://github.com/SanRenSei/ATOM/blob/main/docs/Operations.md).
