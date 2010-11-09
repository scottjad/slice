# slice

Write composable slices of html, css, and js in Clojure

## Usage

See example.clj

## Installation

Search clojars.org for latest version and add it to project.clj or pom.xml

## Overview

A slice is a function that returns a map with keys [:title :html :css :js :dom
:head ...]

        (slice example
          jquery
          (title "Example")
          (js (alert "Hi"))
          (html [:h1 "Bye"])
          (css (rule "h1" :color :blue)))

One difference between slices and normal functions is that the return value of
every form in the body of a slice will be added to the return map. See tricks
for other differences.

So the slice above will return a map like {:slice true, :head ["code for
jquery"], :title "Example", :js ["alert(\"Hi\")"], :html ["<h1>Bye</h1>"], :css
...}.

The function render will render a slice as an html page with the included js
and css.

(render example)

When a slice uses another slice, the maps are merged. In the example above,
jquery is a slice that defines where to get jquery and how to add it to a page.

       (slice jquery [& [version]]
         (head (page-helpers/include-js
               (str "http://ajax.googleapis.com/ajax/libs/jquery/" (or version "1.4.2") "/jquery.min.js"))))

See example.clj for what slices look like in practice.

## Tricks

- Slices don't need to have an arg list if they take 0 args.

- If you're not passing any arguments to a slice, you can omit the enclosing ()
  in render and other slices that use the slice.

- You can stack titles

- Dependencies will only be included once.

## License

Copyright (C) 2010 Scott Jaderholm

Distributed under the GNU General Public License v 3.
