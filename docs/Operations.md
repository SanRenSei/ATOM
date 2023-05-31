# Operations

## . (Dereference)

When applied with an array and a number, it will return the element at that position in the array, or set a value at that array index.

If the index is negative, then it will start from the other end of the array (1-indexed)

If the left operand is undefined, it will be cast into an empty array.

```
[1,2,3].0
# Returns 1

a = [1,2,3];
a[3] = 4;
a
# a will be [1,2,3,4]

[1,2,3].-1
# Returns 3

a = [1,2,3];
a.-1 = 5;
a
# a wil be [1,2,5]

c.0 = 5;
# c will be [5]

```

If applied on a controller, it will locate a matching condition in the controller and return the corresponding branch. If there are no matching conditions, then a default branch will be returned if present.

Similarly to an array, if the left operand is undefined, it will be cast into an empty object.

```
a = {"foo":"bar"};
a."foo"
# Returns "bar"

b."foo" = "bar";
b
# b will be {"foo":"bar"}
```

## 🧵 (Length)

When applied to a string, it will return the length of the string. When applied to an array, it will return the length of the array.

```
🧵"Hello"
# Returns 5

🧵[1,2,3]
# Returns 3
```

## ✂ (Trim)

When applied to a string, it will trim the string, removing trailing whitespace.

```
✂"   World   "
# Returns "World"
```

## 🦶 (Array Flatten)

When applied to an array of arrays, it will flatten it into a larger array. Non-array elements are treated as single-element arrays.

```
🦶[[0,1,2],[3,4,5],[6,7,8]]
# Returns [0,1,2,3,4,5,6,7,8]

🦶[[3,1,4],1,5,[9,2],6]
# Returns [3,1,4,1,5,9,2,6]
```

## ~ (Array Generation or Controller Execution)

If applied to two integer operands, it will returns an array enumerating the range of numbers between them, inclusive.

```
1~5
# Returns [1,2,3,4,5]
```

If applied to a controller, it will execute it.

```
n = 3;
a = {
  n%2==0: "EVEN",
  n%2==1: "ODD"
},
~a
# This will run a and return "ODD"

```

# ! (Boolean negation)

Will return the negation of a boolean

```
!true
# Returns false

!false
# Returns true
```

# * (Multiplication)

Will multiply two numbers. If one of the numbers is null, it will be cast to 0.

If multiplying a string and an integer, it will repeat the string that number of times.

```
5*5
# Returns 25

z*10
# Returns 0

"na"*5
# Returns "nanananana"

```

# / (Division)

Will divide two numbers.  If one of the numbers is null, it will be cast to 0.

If dividing two string, it will perform a string split.

```
5/5
# Returns 1

7/2
# Returns 3

"The quick brown fox jumped over the lazy dog"/" "
# Returns ["The", "quick", "brown", "fox", "jumped", "over", "the", "lazy", "dog"]
```

# % (Modulo)

Will compute the modulo of two numbers.

```
7%2
# Returns 1
```

# + (Addition)

Will add two numbers or strings. If the left operand is an array, it will append the right operand to the array.

```
2+2
# Returns 4

"Hello " + "World"
# Returns "Hello World"

[1,2,3]+4
# Returns [1,2,3,4]
```

# - (Subtraction)

Will subtract two numbers. Nulls are cast to 0.

```
5-1
# Returns 4
```

# \>< (Maximum)

Will compute the maximum of two numbers or strings.

```
3><4
# Returns 4

"Hello"><"World"
# Returns "World"
```

# <> (Minimum)

Will compute the minimum of two numbers or strings.

```
3><4
# Returns 3

"Hello"><"World"
# Returns "Hello"
```

# ==, !=, <, <=, >, >= (Comparisons)

Will perform comparison operations on values

```
2==2
# Returns true

1==2
# Returns false
```

# & (And)

Will perform boolean And operation

```
true&false
# Returns false

true&true
# Returns true
```

# INJECT, ->, => (Injection)

Perform variable injection into a controller. If the left operand is a variable, it will assign a field in the right operand. If the left operand is a controller, it will assign its key/value pairs into the right operand.

```
a = 5;
b = {};
a INJECT b;
b
# b will be {a:5}

c = {"foo":"bar"}
d = {"biz":"baz"};
c -> d;
# d will be {"biz":"baz","foo":"bar"}
```

# = (Assignment)

Performs assignment to a variable

```
a=5
# Assigns the value 5 to variable named a
```

# 🖨, PRINT (Printing)

Prints a value to the console

```
🖨10
# Prints 10 to the console
```

# INTO (Controller execution)

Passes the left operand into the right controller, executes it, and returns the result.

```
a = 3;
b = {
  *%3==0:"Fizz",
  *%5==0:"Buzz",
  *
};
a INTO b;
# Returns "Fizz"

5 INTO b
# Returns "Buzz"
```

# ∀, FOREACH (For loop)

Takes the left operand as an array and passes it into the right operand as a controller, running a for each loop iterating through the array values.

```
[3,1,4,1,5] ∀ {🖨*}
# Prints each of the values in the array (3,1,4,1,5)
```

# 🔢, i∀, iFOREACH (Indexed for loop)

Performs a for loop, but instead of passing the array values into the controller, it passes the array indeces. 

```
[3,1,4,1,5] ∀ {🖨*}
# Prints each of the indeces in the array (0,1,2,3,4)
```

# 🗺, MAP (Array map)

Takes the left operand as an array and passes it into the right operand as a controller, iterating through the array values, and returning an array of the results.

```
[3,1,4,1,5] 🗺 {*+1}
# Returns an array of values that are 1 higher than the original array i.e. [4,2,5,2,6]
```

# 🔍, WHERE (Array filter)

Takes the left operand as an array and passes it into the right operand as a controller, iterating through the array values, and returning an array of the array values that cause the controller to return a truthy value.

```
[3,1,4,1,5] 🔍 {*>2}
# Returns an array of values in the array greater than 2 i.e. [3,4,5]
```

# i🔍, iWHERE (Array index filter)

Takes the left operand as an array and passes it into the right operand as a controller, iterating through the array indeces, and returning an array of the array indeces that cause the controller to return a truthy value.

```
[3,1,4,1,5] 🔍 {*%2==1}
# Returns an array of odd indeces in the array i.e. [1,3]
```

# 🕳, THROUGH (While loop)

Takes the left operand and passes it into the right operand as a controller. Takes the result, and if it is truthy, passes it back into the controller. Continues until the returned value is not truthy.

```
n = 0;
5 🕳 {
  n+=*;
  *-1
}
# n will be 15 after this runs
```

# 🎒, UNPACK

Unpacks the left operand as an object. The right operand denotes the keys that are unpacked from the object into locally scoped variables.

```
a = {"foo":"bar"};
a UNPACK foo;
foo
# The value of variable foo wil be "bar"

b = {"foo":"bar","biz":"baz"};
b 🎒 {foo, biz};
foo+biz
# Both variables will be unpacked, so the values will be added, returning "barbaz"
```

# +=. -=. *=, /=, ><=, <>= (Compound Evaluation Assignments)

Performs an operation with a variable on the left and an operand on the right. Assigns the result back to the variable.

```
a = 3;
a+=1;
# a will be 4
a-=2;
# a will be 2
a*=5;
# a will be 10
a/=2
# a will be 5
a><=7
# a will be 7
a<>=4
# a will be 4
```