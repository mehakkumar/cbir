# cbir
content based image retrieval (search by image)
Designed and developed a software using java, javafx and matlab to search for images in a system via image content-color. CBIR is a major
research topic in the field of computer vision. Worked upon sift algorithm and histogram search.


This repository contains code for histogram search.

In order to search an image, we need histograms for all images to compare. Scan system option looks for all jpgs in specified directory and creates a histogram for each, which are then compared with the desired image to find closest results.
Two images are said to be close, if their ARGB components have similar values.
