# CodRep 2019: Machine Learning on Source Code Competition

The goal of the competition is provide different communities (machine learning, software engineering, programming language) with a common playground to test and compare ideas.
The competition is designed with the following principles:

1. There is no specific background or skill requirements on program analysis to understand the data.
2. The systems that use the competition data can be used beyond the competition itself. In particular, there are potential usages in the field of automated program repair.   

To take part to the competition, you have to write a program which predicts the location in a source code file where there is a formatting error.

For instance, in the following snippet, the system should predict that there should be no space before the semin-column:

```diff
public class test{
  int a = 1 ;
}
```

More specifically, the program  takes as input a source code file, and outputs the predicted character number where the formatting error is. The formatting error has been detected by the Checkstyle tool.

The competition is organized by KTH Royal Institute of Technology, Stockholm, Sweden. The organization team is Benjamin Loriot, [Zimin Chen](https://www.kth.se/profile/zimin) and [Martin Monperrus](http://www.monperrus.net/martin/).

To be get news about CodRep and be informed about the next edition, register to the CodRep mailing list:
[codrep+subscribe@googlegroups.com](mailto:codrep+subscribe@googlegroups.com)

## CodRep Leaderboard

Here is the current CodRep ranking based on Dataset5 (lower score is better).

| # | Team (Institution/Company) | Score | Tool/Source |
| --- | --- | --- | --- |

## CodRep Rules

The official ranking is computed based on a hidden dataset, which is not public or part of already published datasets. In order to maintain integrity, the hash or the encrypted version of the hidden dataset is uploaded beforehand.

## Data Structure and Format

### Format
The provided data are in `Datasets/.../*.txt`. The txt files are meant to be parsed by competing programs. Each txt file corresponds to one prediction task, and the correct answer to each prediction task is at line `n+1` in `Datasets/.../output.txt`, where `n.txt` is the name of prediction task.

For instance, let's consider this example input file, called `0.txt`.
```java
public class test{
  int a = 1 ;
  int b = 0.1;
}
```
The correct answer is at line 1 in `output.txt`, which is 32 (unnecessary space).

## Data provenance

The data used in the competition is generated from open-source projects having one Checkstyle configuration.

The testing data for the intermediate and final score are made on different projects than the ones provided.
Main Statistics about the data:

| Directory | Total source code files | Lines of code (LOC) |
| --- | --- |--- |


## Command-line interface

To play in the competition, your program takes as input input a folder name, that folder containing input data files (per the format explained above).

```shell
$ your-predictor Files
```

Your programs outputs on the console, for each task, the predicted character number. By convention, character numbers start from 1, characters are utf-8 ones (i.e. they can be composed of one or two bytes). If there is no prediction made for certain task (by not outputting *\<path\> \<char number\>*), you will receive maximum loss (which is 1) for the task, more information about this in **Loss function** below.

```
<Path1> <char number>
<Path2> <char number>
<Path3> <char number>
...
```

E.g.;
```
/Users/foo/bar/CodRep-competition/Datasets/Dataset1/Tasks/1.txt 42
/Users/foo/bar/CodRep-competition/Datasets/Dataset1/Tasks/2.txt 78
/Users/foo/bar/CodRep-competition/Datasets/Dataset1/Tasks/3.txt 30
...
```

## How to evaluate your competing program

You can evaluate the performance of your program by piping the output to `Baseline/evaluate.py`, for example:
```shell
your-program Files | python evaluate.py
```

The output of `evaluate.py` will be:
```
Total files: 15463
MAP: 0.988357635773 (the lower, the better)
```

For evaluating specific datasets, use [-d] or [-datasets=] options and specify paths to datasets. The default behaviour is evaluating on all datasets. The path must be absolute path and multiple paths should be separated by `:`, for example:
```shell
your-program Files | python evaluate.py -d /Users/foo/bar/CodRep-competition/Datasets/Dataset1:/Users/foo/bar/CodRep-competition/Datasets/Dataset2
```

Explanation of the output of `evaluate.py`:
* `Total files`: Number of prediction tasks in datasets
* `MAP`: A measurement of the errors of your prediction, as defined in **Loss function** below. This is the only measure used to win the competition

## Loss function

The loss function, used to output a loss that represents the performance of your predictor, is Mean Average Precision (MAP). The lower the loss is, the better are your predictions.

Average precision for one prediction task is defined as `1 / rank(p)`, where `p` is position of formatting error and `rank(p)` is the rank of `p` returned by your predictor. MAP is the mean of average precision across all prediction tasks.

## Base line systems

We provide 5 dumb systems for illustrating how to parse the data and having a baseline performance. These are:
* `guessFirst.py`: Always predict the first character of the file
* `guessMiddle.py`: Always predict the character in the middle of the file
* `guessLast.py`: Always predict the last character of the file
* `randomGuess.py`: Predict a random character in the file
* `maximumError.py`: Predict the worst case, the farthest character from the correct solution

Thanks to the design of the loss function, `guessFirst.py`, `guessMiddle.py`, `guessLast.py` and `randomGuess.py` have the same order of magnitude of error, therefore the value of `Average line error` are comparable.


Dates
=====


* Official competition start: April 14th 2019.
* Submission deadline for intermediate ranking: July 4th 2019.
* Announcement of the intermediate ranking: July 14th 2019.
* Final submission deadline: Oct. 4th 2019.
* Announcement of the final ranking & end of the competition Oct 14th 2019.
