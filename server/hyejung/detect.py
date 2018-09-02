#!/usr/bin/env python

# Copyright 2017 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""This application demonstrates how to perform basic operations with the
Google Cloud Vision API.

Example Usage:
python detect.py text ./resources/wakeupcat.jpg
python detect.py labels ./resources/landmark.jpg
python detect.py web ./resources/landmark.jpg
python detect.py web-uri http://wheresgus.com/dog.JPG
python detect.py web-geo ./resources/city.jpg
python detect.py faces-uri gs://your-bucket/file.jpg
python detect_pdf.py ocr-uri gs://python-docs-samples-tests/HodgeConj.pdf \
gs://BUCKET_NAME/PREFIX/

For more information, the documentation at
https://cloud.google.com/vision/docs.
"""

import argparse
import io
import re

from google.cloud import storage
from google.cloud import vision
from google.protobuf import json_format




def MY_detect_labels(path):
    """Detects labels in the file."""
    client = vision.ImageAnnotatorClient()

    # [START migration_label_detection]
    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)

    response = client.label_detection(image=image)
    labels = response.label_annotations

    link_array=[]

    for label in labels:
        temp_link={"description":label.description, "score":label.score}
        link_array.append(temp_link)

    return link_array

def MY_detect_labels_URL(uri):
    """Detects labels in the file."""
    client = vision.ImageAnnotatorClient()
    image=vision.types.Image();
    image.source.image_uri=uri

    response=client.label_detection(image=image)
    labels = response.label_annotations

    link_array=[]

    for label in labels:
        temp_link={"description":label.description, "score":label.score}
        link_array.append(temp_link)

    return link_array


# [START def_detect_landmarks]
def MY_detect_landmarks(path):
    """Detects landmarks in the file."""
    client = vision.ImageAnnotatorClient()

    # [START migration_landmark_detection]
    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)

    response = client.landmark_detection(image=image)
    landmarks = response.landmark_annotations

    link_array=[]

    for landmark in landmarks:
        temp_link={'description':landmark.description}
        for location in landmark.locations:
            lat_lng = location.lat_lng
            temp_link['Latitude']=lat_lng.latitude
            temp_link['Longitude']=lat_lng.longitude
            link_array.append(temp_link)


    return link_array

    # [END migration_landmark_detection]
# [END def_detect_landmarks]


def MY_detect_landmarks_URL(uri):
    """Detects landmarks in the file."""
    client = vision.ImageAnnotatorClient()
    image = vision.types.Image()
    image.source.image_uri = uri

    response = client.landmark_detection(image=image)
    landmarks = response.landmark_annotations

    link_array=[]

    for landmark in landmarks:
        temp_link={'description':landmark.description}
        for location in landmark.locations:
            lat_lng = location.lat_lng
            temp_link['Latitude']=lat_lng.latitude
            temp_link['Longitude']=lat_lng.longitude
            link_array.append(temp_link)


    return link_array



# [START def_detect_properties]
def MY_detect_properties(path):
    """Detects image properties in the file."""
    client = vision.ImageAnnotatorClient()

    # [START migration_image_properties]
    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)

    response = client.image_properties(image=image)
    props = response.image_properties_annotation

    link_array=[]

    for color in props.dominant_colors.colors:
        temp_link={'pixel':color.pixel_fraction,'color-red':color.color.red,'color-green':color.color.green, 'color-blue':color.color.blue}
        link_array.append(temp_link)

    return link_array
    # [END migration_image_properties]
# [END def_detect_properties]

def MY_detect_properties_URL(uri):
    """Detects image properties in the file."""
    client = vision.ImageAnnotatorClient()
    image = vision.types.Image()
    image.source.image_uri = uri

    response = client.image_properties(image=image)
    props = response.image_properties_annotation

    link_array=[]

    for color in props.dominant_colors.colors:
        temp_link={'pixel':color.pixel_fraction,'color-red':color.color.red,'color-green':color.color.green, 'color-blue':color.color.blue}
        link_array.append(temp_link)

    return link_array


# [START def_detect_web]
def MY_detect_web(path):
    """Detects web annotations given an image."""
    client = vision.ImageAnnotatorClient()

    # [START migration_web_detection]
    with io.open(path, 'rb') as image_file:
        content = image_file.read()

    image = vision.types.Image(content=content)

    response = client.web_detection(image=image)
    annotations = response.web_detection

    link_array1=[]

    if annotations.best_guess_labels:
        for label in annotations.best_guess_labels:
            temp_link={'top label':(label.label).encode('utf-8')}
            link_array1.append(temp_link)

    link_array2=[]

    if annotations.web_entities:
        for entity in annotations.web_entities:
            temp_link={'description':entity.description, 'score':entity.score}
            link_array2.append(temp_link)

    return (link_array1, link_array2)
    # [END migration_web_detection]
# [END def_detect_web]

def MY_detect_web_URL(uri):
    """Detects web annotations given an image."""
    client = vision.ImageAnnotatorClient()
    image = vision.types.Image()
    image.source.image_uri = uri

    response = client.web_detection(image=image)
    annotations = response.web_detection

    link_array1=[]

    if annotations.best_guess_labels:
        for label in annotations.best_guess_labels:
            temp_link={'top label':(label.label).encode('utf-8')}
            link_array1.append(temp_link)

    link_array2=[]

    if annotations.web_entities:
        for entity in annotations.web_entities:
            temp_link={'description':entity.description, 'score':entity.score}
            link_array2.append(temp_link)

    return (link_array1, link_array2)
