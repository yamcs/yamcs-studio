# -*- coding: utf-8 -*-
#
# Configuration file for the Sphinx documentation builder.
#
# This file does only contain a selection of the most common options. For a
# full list see the documentation:
# http://www.sphinx-doc.org/en/master/config

import os
import sys
from xml.etree import ElementTree as ET

# If extensions (or modules to document with autodoc) are in another directory,
# add these directories to sys.path here. If the directory is relative to the
# documentation root, use os.path.abspath to make it absolute, like shown here.
sys.path.insert(0, os.path.abspath("_ext"))

# Read the latest Yamcs versions from the Maven pom.xml
tree = ET.ElementTree()
tree.parse("../pom.xml")
yamcs_version_el = tree.getroot().find("{http://maven.apache.org/POM/4.0.0}version")

project = u"Yamcs Studio"
copyright = u"2019-2021, Space Applications Services"
author = u"Yamcs Team"

# The short X.Y version
version = yamcs_version_el.text

# The full version, including alpha/beta/rc tags
release = version

extensions = [
    "sphinxcontrib.fulltoc",
    "sphinx.ext.intersphinx",
    "color",
    "opi",
]

# Add any paths that contain templates here, relative to this directory.
# templates_path = ['_templates']

# The suffix(es) of source filenames.
# You can specify multiple suffix as a list of string:
#
# source_suffix = ['.rst', '.md']
source_suffix = ".rst"

# The language for content autogenerated by Sphinx. Refer to documentation
# for a list of supported languages.
#
# This is also used if you do content translation via gettext catalogs.
# Usually you set "language" from the command line for these cases.
language = None

# List of patterns, relative to source directory, that match files and
# directories to ignore when looking for source files.
# This pattern also affects html_static_path and html_extra_path .
exclude_patterns = [u"_build", "Thumbs.db", ".DS_Store"]

# The name of the Pygments (syntax highlighting) style to use.
pygments_style = "sphinx"

# The theme to use for HTML and HTML Help pages.  See the documentation for
# a list of builtin themes.
#
html_theme = "alabaster"

# Theme options are theme-specific and customize the look and feel of a theme
# further.  For a list of options available for each theme, see the
# documentation.
#
html_theme_options = {
    #'description': 'Yamcs Studio',
    "fixed_sidebar": False,
    "show_powered_by": False,
    "font_family": "Helvetica,Arial,sans-serif",
    "font_size": "15px",
}

# Add any paths that contain custom static files (such as style sheets) here,
# relative to this directory. They are copied after the builtin static files,
# so a file named "default.css" will overwrite the builtin "default.css".
html_static_path = ["_static"]

html_show_sourcelink = False

latex_elements = {
    "papersize": "a4paper",
    "preamble": r"""
      \newcolumntype{\Yl}[1]{>{\raggedright\arraybackslash}\Y{#1}}
      \newcolumntype{\Yr}[1]{>{\raggedleft\arraybackslash}\Y{#1}}
      \newcolumntype{\Yc}[1]{>{\centering\arraybackslash}\Y{#1}} 
    """,
    "figure_align": "htbp",
}

# Grouping the document tree into LaTeX files. List of tuples
# (source start file, target name, title,
#  author, documentclass [howto, manual, or own class]).
latex_documents = [
    (
        "index",
        "yamcs-studio.tex",
        "Yamcs Studio User Guide",
        "Space Applications Services",
        "manual",
    ),
]

latex_show_pagerefs = True

latex_show_urls = "footnote"


def setup(app):
    app.add_css_file("style.css")
