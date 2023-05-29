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

