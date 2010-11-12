# slice

Write composable slices of html, css, and js in Clojure.

Slice uses Scriptjure, Hiccup, and Cssgen for the hard stuff.

## Motivation

I've been writing webapps entirely in Clojure for some time, using scriptjure
to generate JS, hiccup for HTML, and cssgen for CSS. I was tired though of not
having all the code for a slice of a webpage in one place. Some of my css was
in css.clj and my scriptjure in javascript.clj and my html throughout my
app. If I wanted to add a css class and use it with js I had to change it in at
least three files (html, css, and js). I really wanted to have my html, css,
and js for a given widget right next to each other so they could more easily be
composed, reused, modified, and tested.

## Usage

See [example.clj](https://github.com/scottjad/slice/blob/master/src/slice/example.clj)

## Installation

Search clojars.org for latest version and add it to project.clj or pom.xml.

## Overview

A slice is a function that returns Slice record with keys such as :title,
:html, :css, :js, :dom, :head, etc.

        (slice example
          jquery
          (title "Example")
          (js (alert "Hi"))
          (html [:h1 "Bye"])
          (css (rule "h1" :color :blue)))

One difference between slices and normal functions is that the return value of
every form in the body of a slice will be added to the slice returned. So in the
example above, none of the forms have side effects but css is not the only form
included in the slice returned.

Another difference is that when defining a slice that doesn't take any args
like example above the arg list can be ommited. Also when calling slices that
take no args, like jquery above, they don't need to be enclosed in (). See
Tricks for other differences.

So the slice above defines a function named example that will return a record
like:

        {:slice true
         :head ["code for jquery"]
         :title ["Example"] 
         :js ["alert(\"Hi\")"]
         :html ["<h1>Bye</h1>"]
         :css ...}

The function render will render a slice as an html page with the included js
and css.

        (render example)

To have all slices rendered automatically by compojure just use slice.compojure
like in the example.

When a slice uses another slice, they are merged. In the example above, jquery
is a slice that defines where to get jquery and how to add it to a page.

       (slice jquery [& [version]]
         (head (page-helpers/include-js
               (str "http://ajax.googleapis.com/ajax/libs/jquery/" (or version "1.4.2") "/jquery.min.js"))))

See [example.clj](https://github.com/scottjad/slice/blob/master/src/slice/example.clj) for what slices look like in practice.

## Performance

Slices are assumed to be pure functions and are memoized as are their
renderings. If a slice is not pure it needs to be marked with ^{:impure true}
and it and slices that use it will not be memoized.

Memoization is turned off by default because it interferes with interactive
development. When deploying use:

       (slice-memoize! true)

## Tricks

- Slices don't need to have an arg list if they take 0 args.

- If you're not passing any arguments to a slice, you can omit the enclosing ()
  in render and other slices that use the slice.

- You can stack titles.

- Dependencies will only be included once.

- If you want to combine slices w/o giving them a name you can use the function
  slices.

## TODO
- Write js and css to files

## License

Copyright (C) 2010 Scott Jaderholm

Distributed under the GNU Lesser General Public License v 3.0.
