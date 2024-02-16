# Javagame
Simple unoptimized raycasting game in Java using JavaFX.

![](/screenshots/sc.png?raw=true)

## Building

- Requires maven

#### Download repo:
```console
$ git clone https://github.com/tikis23/javagame.git
$ cd javagame 
```
#### To build a jar run:
```console
$ mvn clean package
```
Note: Running the jar requires folders `maps`, `sprites` and `textures` folders and their contents in the same location as the jar.
#### Run project directly:
```console
$ mvn clean javafx:run
```