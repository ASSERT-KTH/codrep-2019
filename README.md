# CodRep 2019: Machine Learning on Source Code Competition

The goal of the competition is provide different communities (machine learning, software engineering, programming language) with a common playground to test and compare ideas.
The competition is designed with the following principles:

1. There is no specific background or skill requirements on program analysis to understand the data.
2. The systems that use the competition data can be used beyond the competition itself. In particular, there are potential usages in the field of automated program repair.

To take part in the competition, you have to write a program which ranks the character offsets in a source code file according to their likeliness of containing a formatting error.

For instance, in the following snippet, the system should predict that there should be no space before the semicolon by ranking the offset of the space as high as possible (ideally in first position):

```java
public class test{
  int a = 1 ;
}
```

More specifically, the program takes as input a source code file and outputs the predicted ranking of character offsets according to its estimation of their likeliness of containing a formatting error. The formatting error has been detected by the Checkstyle tool.

The competition is organized by KTH Royal Institute of Technology, Stockholm, Sweden. The organization team is Benjamin Loriot, [Zimin Chen](https://www.kth.se/profile/zimin) and [Martin Monperrus](http://www.monperrus.net/martin/).

To get news about CodRep and be informed about the next edition, register to the CodRep mailing list:
[codrep+subscribe@googlegroups.com](mailto:codrep+subscribe@googlegroups.com)

## CodRep Leaderboard

Here is the current CodRep ranking based on Dataset5 (lower score is better).

| # | Team (Institution/Company) | Score | Tool/Source |
| --- | --- | --- | --- |

## CodRep Rules

The official ranking is computed based on a hidden dataset, which is not public or part of already published datasets. In order to maintain integrity, the hash or the encrypted version of the hidden dataset is uploaded beforehand.

## Data Structure and Format

### Format
The provided data are in `Datasets/.../*.txt`. The txt files are meant to be parsed by competing programs. Each txt file corresponds to one prediction task, and the offset of the style error for each prediction task is at line `n+1` in `Datasets/.../output.txt`, where `n.txt` is the name of prediction task.

For instance, let's consider this example input file, called `0.txt`.
```java
public class test{
  int a = 1 ;
  int b = 0.1;
}
```
The location of the style error is at line 1 in `out.txt`. Here, it would be 30 (unnecessary space).

## Data provenance

The data used in the competition is generated from open-source projects having one Checkstyle configuration.

The testing data for the intermediate and final score are made on different projects than the ones provided.
Main Statistics about the data:

| Directory | Total source code files | Lines of code (LOC) |
| --- | --- |--- |


## Command-line interface

To play in the competition, your program takes as input a folder name, that folder containing input data files (per the format explained above).

```shell
$ your-predictor Files
```

Your programs outputs on the console, for each task, the predicted offset ranking. By convention, character offsets start from 1, characters are utf-8 ones (i.e. they can be composed of one to four bytes). If there is no prediction made for a certain task (by not outputting *\<path\> \<offset 1\> … \<offset n\>*), you will receive minimum score (which is 0) for the task, more information about this in **Evaluation metric** below.

```
<Path1> <offset 1> <offset 2> … <offset n1>
<Path2> <offset 1> <offset 2> … <offset n2>
<Path3> <offset 1> <offset 2> … <offset n3>
...
```

Where `n1`, `n2` and `n3` are the number of characters the considered files.

E.g.;
```
/Users/foo/bar/CodRep-competition/Datasets/Dataset1/Tasks/1.txt 212 41 13 …
/Users/foo/bar/CodRep-competition/Datasets/Dataset1/Tasks/2.txt 18 33 25 …
/Users/foo/bar/CodRep-competition/Datasets/Dataset1/Tasks/3.txt 56 37 11 …
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
MAP: 0.988357635773 (the higher, the better)
```

To evaluate specific datasets, use [-d] or [-datasets=] options and specify paths to datasets. The default behaviour is evaluating on all datasets. The path must be absolute path and multiple paths should be separated by `:`, for example:
```shell
your-program Files | python evaluate.py -d /Users/foo/bar/CodRep-competition/Datasets/Dataset1:/Users/foo/bar/CodRep-competition/Datasets/Dataset2
```

Explanation of the output of `evaluate.py`:
* `Total files`: Number of prediction tasks in datasets
* `MAP`: A measurement of the errors of your prediction, as defined in **Loss function** below. This is the only measure used to win the competition

## Evaluation metric

The evaluation metric, used to output a score that represents the performance of your predictor, is Mean Average Precision (MAP). The higher the score is, the better are your predictions.

Average precision for one prediction task is defined as `1 / rank(p)`, where `p` is position of formatting error and `rank(p)` is the rank of `p` returned by your predictor. MAP is the mean of average precision across all prediction tasks.

## Baseline systems

We provide 3 dumb systems to illustrate how to parse the data and having a baseline performance. These are:
* `guessSorted.py`: Always predict the offsets of the file in increasing order
* `guessReversed.py`: Always predict the offsets of the file in decreasing order
* `guessRandom.py`: Predicts a random ranking of the offsets in the file

# Dates

* Official competition start: April 14th 2019.
* Submission deadline for intermediate ranking: July 4th 2019.
* Announcement of the intermediate ranking: July 14th 2019.
* Final submission deadline: Oct. 4th 2019.
* Announcement of the final ranking & end of the competition Oct 14th 2019.
