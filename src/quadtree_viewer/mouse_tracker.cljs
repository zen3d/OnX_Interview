(ns quadtree-viewer.mouse-tracker
  (:require [quadtree-viewer.geometry :as geometry])
)

;; Create a camera that tracks mouse movement;
;; There might be better ways to do this in THREE.js, but the cost of writing it
;; was less than figuring out how to do it in THREE.js.
(def camera nil)

;; Scale factors
(def x-scale -0.001)
(def y-scale 0.001)
(def z-scale 0.98)

;; Mouse state
(def x 0)
(def y 0)
(def is-moving false)

;; Scroll wheel state
(def z 1)

;; camera-update - update camera to reflect the state of the mouse and scroll wheel.
(defn camera-update []
  (let [
    position (. camera -position)
    x-new (+ (* x x-scale) (.. position -x))
    y-new (+ (* y y-scale) (.. position -y))
    ]
    (set! (.. position -x) x-new)
    (set! (.. position -y) y-new)
    (set! (.. position -z) z)
    ;;(println "camera position: " (.. position -x) (.. position -y) (.. position -z))
    (set! x 0)
    (set! y 0)
    ;;(set! z 1)
  )
)

;; Mouse event handler.
(defn mousemove-handler [evt]
  (let [
    dx (. evt -movementX)
    dy (. evt -movementY)
    ]
    (if is-moving
      (do
        (set! x (+ x dx))
        (set! y (+ y dy))
        (camera-update)
        ;;(println "mouse move at (" x ", " y ")")
        )
      )
    )
  )

;; Mouse event handler.
(defn mousedown-handler [evt]
  (do
    (set! x 0)
    (set! y 0)
    (camera-update)
    (set! is-moving true)))

;; Mouse event handler.
(defn mouseup-handler [evt]
  (let [
    dx (. evt -movementX)
    dy (. evt -movementY)
    ]
    (do
      (set! is-moving false)
      (set! x (+ x dx))
      (set! y (+ y dy))
      (camera-update)
      ;;(println "mouse up at: (" x " ," y ")")
    )))

;; Scroll wheel event handler.
;; Scroll wheel tracks the y coordinate but modifies the camera's z coordinate.
(defn scroll-handler [evt]
  (do
    (set! z (min 1 (if (> 0 (+ z (. evt -deltaY))) (* z z-scale) (/ z z-scale))))
    (camera-update)
    ;;(println "scroll: " z)
  ))

;; Create camera and  capturing mouse/scroll events.
(defn make-camera [target]
  (.addEventListener target "wheel" scroll-handler)
  (.addEventListener target "mousemove" mousemove-handler)
  (.addEventListener target "mousedown" mousedown-handler)
  (.addEventListener target "mouseup" mouseup-handler)
  )

;; Get current state of THREE.js camera.
(defn track-camera [threejs-camera target]
  (set! camera threejs-camera)
  (make-camera target)
  )
