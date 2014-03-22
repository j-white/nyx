[![Build Status](https://travis-ci.org/j-white/nyx.png?branch=master)](https://travis-ci.org/j-white/nyx)

# Nyx #

## What is Nyx?

Nyx is a Java implementation of a **fractal based signal encoding and decoding** algorithm.

The algorithm tries to represent the given signal using a collection of contractive maps. This representation has the advantage that is resolution independent and can be stored compactly, if compression is the goal, however the encoding process is computationally intensive.

An overview of fractal compression is available here: [Fractal compression on Wikipedia](http://en.wikipedia.org/wiki/Fractal_compression). Further details can be found in a number of academic papers and publishing [Fractal compression on Google Scholar](http://scholar.google.ca/scholar?hl=en&q=fractal+image+compression)
## Building Nyx

### Dependencies

This project requires [Java 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html) and [Maven 3](http://maven.apache.org/).

### Compiling

Running

    $ mvn compile

will compiler the source, run the unit tests and generate a super JAR (containing all of the required dependencies).

## Using Nyx

### Running

From the root of the project, you can trying encoding and decoding an image at various scales from the with:

    $ mkdir output
    $ java -jar target/nyx-1.0.0-executable.jar -s 2,4,8 -o output/ samples/fractal-256x256-gray.jpg

Running the previous command will make all of the CPU cores scream for several minutes and will placed the generated files in the `output/` folder.

The output folder will contain a series of decoded images (at scales 1x, 2x, 4x and 8x) and a LaTeX based report giving insights into the encoding and decoding processes.

If you experience out of memory errors when decoding at higher scales you will need to increase the heap size on your JVM. You can increase the heap from the default to 8GB by adding the `-Xmx8g`  flag before the `-jar` in the command above.

## Current issues

- Scaling colour images does not work (the re-scaled channels don't align properly)
- Scaling is limited by the amount of memory available, we should be able to use disk space to store the matrix as well.
- Encoding performance is poor.

## Example results

### Lena

Here's a standard example using the algorithm to encode and decode an image of Lena:

    $ java -jar target/nyx-1.0.0-executable.jar -o output/ samples/lena-256x256-gray.png

**lena-256x256-gray.png:**

![](https://raw.githubusercontent.com/j-white/nyx/master/samples/lena-256x256-gray.png) 

**decoded-1x-lena-256x256-gray.png:**

![](https://raw.githubusercontent.com/j-white/nyx/master/examples/decoded-1x-lena-256x256-gray.png) 

### Resolution independence

Here we encode a tiny image (32x32 pixels) and decode it at 64x (2048x2048), the results are quite interesting:

    $ java -Xmx1g -jar target/nyx-1.0.0-executable.jar -s 64 samples/tiny-32x32-gray.jpg

**tiny-32x32-gray.jpg:**

![](https://raw.githubusercontent.com/j-white/nyx/master/samples/tiny-32x32-gray.jpg) 

**decoded-1x-tiny-32x32-gray.jpg:**

![](https://raw.githubusercontent.com/j-white/nyx/master/examples/decoded-1x-tiny-32x32-gray.jpg) 

**decoded-64x-tiny-32x32-gray.jpg:**

![](https://raw.githubusercontent.com/j-white/nyx/master/examples/decoded-64x-tiny-32x32-gray.jpg)
