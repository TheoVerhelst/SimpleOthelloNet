# SimpleOthelloNet

## What's this?
This is a simple attempt to use a neural network to learn how to play the [Othello game](https://en.wikipedia.org/wiki/Reversi).
It turns out that the neural network beats a minimax player at depth of 6 with a good heuristic designed by T. Yoshioka
(see the doc for further references).

## How does it work?
The network is trained by modifying its weights using temporal difference learning. It plays against a copy of itself, and takes
the raw board as input. After a few 45,000 games, it becomes a decent player. See the doc for further explanations.

## How can I try it?
It's pure java, with only the help of Neuroph library. Once you set up the project in your IDE and imported Neuroph,
just run it. In the main function, you can either run

```java
  NetworkTrainer.train();
```
to train the neural network (takes a long time), or
```java
  Minimax.testMinimax();
```
to play yourself against the minimax player in the terminal.
With some code editing, you could also play against the neural network.
