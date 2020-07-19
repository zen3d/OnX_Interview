<h1>onXmaps Interview Challenge</h1>

This project is a solution to the onXmaps Interview Challenge. It solves the
problem of viewing and navigating through a hierarchical quadtree data structure.

<h2>Quadtrees</h2>

A quadtree is a hierarchical data structure that decomposes a square into four
children, each which are smaller squares that have an edge length half that of the parent.
Any location in the top level square can be specified to an arbitrary level of
precision by encoding the path from the root to the leaf that meets the
precision requirement.

A node in the quadtree encodes the bounding box of the node, pointers to the
nodes of its four children, and an optional domain value for the node.
Technically, the bounding box does not need to be captured since it can be
derived from the traversal path for a node from the root. However, capturing the bounding box is a minor
convenience. The domain value can be used to capture level-of-detail information,
such as texture maps (which might be used to create a "<em>mipmap</em>"), or geometric
models of increasing complexity.

These paths are generally encoded with two bit quad keys, which can represent
the four children of the parent. By convention, the lowest quad key is
represented in the least significant bits of an integer, and higher level quad
keys are prepended in the direction of the most significant bits. Since 0 is a
valid quad key, the string of bits is usually prepended with a 1 bit to delimit
the bit stream.

This quadtree data structure was used by Microsoft's Virtual Earth mapping
product, among others.

<h2>Navigation</h2>

The specification for this project stated that the solution needed to provide
navigation for both panning and zooming.

Panning is performed by dragging the mouse (pushing the mouse button, moving the
  mouse, and releasing the mouse button).

Zooming is performed using the scroll wheel. Scrolling down zooms in, revealing
a more detailed child, and scrolling up zooms out, revealing a less detailed child.

Internally, the scrolling code manages a Z value in the range of 0..1. The value
1 corresponds to the top level of the quadtree. Zooming in scales the current Z
value by a constant slightly less than 1. Conversely, zooming out scales the current Z value by a constant slightly larger than 1, with clamping of the Z value when it hits 1. In the limit, this will descend to
deeper levels as Z approaches 0. The Z value can be converted to a quadtree
hierarchy depth by computing:  
<blockquote><code>depth = -floor(log2(Z))</code></blockquote>  
Note that Z implicitly encodes the quadtree depth, which is never explicitly
encoded.

<h2>Use of ClojureScript</h2>

Since I have previously written quadtree geospatial encoders, I wanted to
do something Completely Different (TM Monty Python) for this project. In a former
life, I had written a lot of code in Scheme, a Lisp dialect. I have also dabbled
with Clojure, a Lisp dialect that compiles to the JVM (as well as .NET). Since
a web browser was a valid target for this project, I decided to write the code in
ClojureScript, a Lisp dialect closely related to CLoure that compiles to JavaScipt
which can run in a browser and which has the ability to interoperate with JavaScript outside the code base (e.g., THREE.js libraries). This project is my first use of ClojureScript, although prior exposure to Clojure has helped.

<h2>Use of THREE.js for WebGL access</h2>

In order to potentially do a 3D implementation that runs in a browser, I used
the THREE.js library to provide a more rational interface t the underlying
WebGL. I have previously written both raw WebGL code and code based on THREE.js,
and I prefer the latter for it's ability to abstract away the gory details of
WebGL, which essentially is a JavaScript binding for OpenGL ES2.

<h2>Building and running</h2>
<p>The code is built by executing make.sh in a user shell.</p>
<p>The code can be run by opening index.html is a browser.</p>

All code was built and tested under Linux. It may or may not build or run correctly on Windows or on MacOS. YMMV.

<h2>Open issues</h2>
The following issues would be addressed if this were of product quality:
<ol>
<li>The pan motion is scaled by the Z depth. This is wrong and results in panning
that is too twitchy the deeper you descend. Instead, pan motion should be scaled
by the log of Z.</li>

<li>Height and width of the browser window are not accounted for correctly. This
may result in too few quads being displayed if the window is too wide or too
tall.</li>

<li>Quad key information is not displayed in the browser window. However, it is
provided in debug spew that appears in the browser console window as text.</li>
<li>While there is no hard limit on how deeply the user can descend into a quadtree, in practice, JavaScript garbage collection dominates performance if you go too deep. I have some ideas on how to mitigate this effect, but they remain unimplemented.</li>
<li>Proper unit tests are needed. I added informal tests scattered throughout the core (a.k.a., main) module to validate code that I was uncertain about. However, this is not a proper substitute for formal unit tests.</li>
<li>The code does not adhere to the standard Clojure/ClojureScript style guide. It deviates most significantly on indentation. I like my code to visually reflect the hierarchical block structure, so I place closing parentheses directly below their corresponding opening parentheses (unless all closing parentheses fit onto the same line as the opening parentheses). The same formatting applies to brackets.
</li>
<li>Create a proper makefile.</li>

</ol>
