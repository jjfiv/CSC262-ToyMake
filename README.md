# CSC262 "Doit" simplified Make

My simple make language currently executes rules in order they are found, whether they are requested or not. What we need to do is to actually do some graph reasoning based on the list of rules we can already load.

## Example doit file:

We support comments starting with '#', and rules that produce files based on some number of inputs (after '<-' but not required) and a bash command (after ':').

```
# command to create a file called timestamp with this time.
timestamp <- 
  : date '+%Y-%m-%d %H:%M:%S' > timestamp

# count the lines of code we have written
counts <- src/main/java/doit/Main.java 
  : wc -l src/main/java/doit/*.java > counts

run_test <- test.c
  : gcc -o run_test test.c && ./run_test a b c
```

The Rule class knows how to write commands to a temporary file and execute them. What we want to do is come up with some more graph-like files:

e.g., ABC:
```
a <- b c : cat b c > a
b <- : echo "This is B" > b
c <- : echo "This is C" > c
```

Here, we must execute b and c before we can execute a, but a is listed first. We must therefore have some more complicated reasoning about how to execute rules.

## What to do, in what order?

Let's get organized. Right now we have a ``List<Rule>`` that is our graph. Let's put that in a class.

### (=10) Rule class updates

 - We need to be able to ignore rules that are "done". Targets that are done have files with timestamps newer than all those of their inputs. The File class in Java has a way to get this info.
 - We should report an error if, after running a command, the target file has not been created.

### (=40) Graph class
 
 - We need to find all the rules that are not done.
 - From those rules, we need to find all the rules that are ready.
 - Rules that are "ready" have all of their inputs existing or "done" if their inputs come from other rules. This feels like it belongs on the Rule class, but needs input from the Graph.
 - We want to run rules that are ready and refresh the ready list.

### (=20) More Graph stuff

 - We should quickly report an error if there are "unreachable" rules; i.e., the files don't exist and there is no way to compute them provided.
 - We should quickly report an error if there is a dependency loop. This is a user error in this kind of program.

 ```
a <- b : echo "A depends on B" > a
b <- c : echo "B depends on C" > b
c <- a : echo "C depends on A" > c 
 ```

### (=30) Multithreading

 - When multiple rules are available at the same time, we want to execute them in parallel.
 - Whenever a job returns, we want to see if any new rules are ready, and launch those.
 - We want to make sure any access to fields on Rule are properly synchronized.
 - When a job fails, we want to cancel all the currently running jobs and report an error to the user.
 
