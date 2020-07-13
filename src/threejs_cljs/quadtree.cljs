(ns threejs-cljs.quadtree
  (:require [threejs-cljs.geometry :as geometry])
)

(defrecord Quadtree [child00 child01 child10 child11 bounds data])

(defn make-point [x y]
  (geometry/Point. x y)
  )

(defn make-quadtree []
  (Quadtree. nil nil nil nil (geometry/make-rect -1 -1 1 1) nil)
  )

(defn private-insert-quadtree [quadtree data x y z x-mid y-mid scale]
  ;; (println " x:" x " y: " y " z: " z " x-mid: " x-mid " y-mid: " y-mid " scale: " scale) ;; DEBUG
  (let
    [
      bounds (geometry/make-rect (- x-mid scale) (- y-mid scale) (+ x-mid scale) (+ y-mid scale))
      valid-quadtree
        (if (nil? quadtree)
          (Quadtree. nil nil nil nil  bounds nil)
          quadtree)
      quadtree-data (:data valid-quadtree)
      half-scale (* 0.5 scale)
    ]
    ;; (println "bounds: " bounds) ;; DEBUG
    (if (>= z 1)
      (Quadtree. (:chil00 valid-quadtree) (:child01 valid-quadtree) (:child10 valid-quadtree) (:child11 valid-quadtree) bounds data)
      (if (< x 0.0)
        (if (< y 0.0)
          ;; Third quadrant: x < 0 and y < 0 ...
          (Quadtree.
            (private-insert-quadtree (:child00 valid-quadtree) data (+ (* x 2) 1) (+ (* y 2) 1) (* z 2) (- x-mid half-scale) (- y-mid half-scale) half-scale)
            (:child01 valid-quadtree)
            (:child10 valid-quadtree)
            (:child11 valid-quadtree)
            (:bounds valid-quadtree)
            quadtree-data
          )
          ;; Second quadrant: x < 0 and y >= 0 ...
          (Quadtree.
            (:child00 valid-quadtree)
            (private-insert-quadtree (:child01 valid-quadtree) data (- (* x 2) 1) (+ (* y 2) 1) (* z 2) (- x-mid half-scale) (+ y-mid half-scale) half-scale)
            (:child10 valid-quadtree)
            (:child11 valid-quadtree)
            (:bounds valid-quadtree)
            quadtree-data
          )
        )
        (if (< y 0.0)
          ;; Fourth quadrant: x >= 0 and y < 0 ...
          (Quadtree.
            (:child00 valid-quadtree)
            (:child01 valid-quadtree)
            (private-insert-quadtree (:child10 valid-quadtree) data (+ (* x 2) 1) (- (* y 2) 1) (* z 2) (+ x-mid half-scale) (- y-mid half-scale) half-scale)
            (:child11 valid-quadtree)
            (:bounds valid-quadtree)
            quadtree-data
          )
          ;; First quadrant: x >= 0 and y >= 0 ...
          (Quadtree.
            (:child00 valid-quadtree)
            (:child01 valid-quadtree)
            (:child10 valid-quadtree)
            (private-insert-quadtree (:child11 valid-quadtree) data (- (* x 2) 1) (- (* y 2) 1) (* z 2) (+ x-mid half-scale) (+ y-mid half-scale) half-scale)
            (:bounds valid-quadtree)
            quadtree-data
          )
        )
      )
    )
  )
)

(defn insert-quadtree [quadtree data x y z]
  (private-insert-quadtree quadtree data x y z 0 0 1)
)

;; seek-quadtree requires a valid node at (x, y, z),
;; which impleies that insert-quadtree should be called
;; prior to calling seek-quadtree;
(defn private-seek-quadtree [quadtree x y z scale]
  ;;(println " x:" x " y: " y " z: " z " scale: " scale)
  (let
    [
      ;; TODO: is generating a valid quadtree needed or will this be populated in provided quadtree?
      valid-quadtree
        (if (nil? quadtree)
          (Quadtree. nil nil nil nil (geometry/make-rect (- x scale) (- y scale) (+ x scale) (+ y scale)) nil)
          quadtree
        )
      quadtree-data (:data valid-quadtree)
      ;; bounds (:bounds quadtree) ;; DEBUG
    ]
    ;; (println bounds) ;; DEBUG
    (if (>= z 1.0)
      quadtree
      (if (< x 0.0)
        (if (< y 0.0)
          ;; Third quadrant: x < 0 and y < 0 ...
          (private-seek-quadtree (:child00 valid-quadtree) (+ (* x 2.0) 1.0) (+ (* y 2.0) 1.0) (* z 2) (* scale 0.5))
          ;; Second quadrant: x < 0 and y >= 0 ...
          (private-seek-quadtree (:child01 valid-quadtree) (- (* x 2.0) 1.0) (+ (* y 2.0) 1.0) (* z 2) (* scale 0.5))
        )
        (if (< y 0.0)
          ;; Fourth quadrant: x >= 0 and y < 0 ...
          (private-seek-quadtree (:child10 valid-quadtree) (+ (* x 2.0) 1.0) (- (* y 2.0) 1.0) (* z 2) (* scale 0.5))
          ;; First quadrant: x >= 0 and y >= 0 ...
          (private-seek-quadtree (:child11 valid-quadtree) (- (* x 2.0) 1.0) (- (* y 2.0) 1.0) (* z 2) (* scale 0.5))
        )
      )
    )
  )
)

(defn seek-quadtree [quadtree x y z]
  (private-seek-quadtree quadtree x y z 1)
)
