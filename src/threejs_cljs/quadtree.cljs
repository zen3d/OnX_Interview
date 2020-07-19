(ns threejs-cljs.quadtree
  (:require [threejs-cljs.geometry :as geometry])
)

(defrecord Quadtree [bounds child00 child01 child10 child11 data])

(defn make-point [x y]
  (geometry/Point. x y)
  )

(defn make-quadtree []
  (Quadtree. (geometry/make-rect -1 -1 1 1) nil nil nil nil nil)
)

;; Form a valid quadtree with bounds.
(defn valid-quadtree [quadtree bounds]
  (if (nil? quadtree)
    (Quadtree. bounds nil nil nil nil nil)
    (if (nil? (:bounds quadtree))
      (Quadtree. bounds (:child00 quadtree) (:child01 quadtree) (:child10 quadtree) (:child11 quadtree) nil)
      quadtree
    )
  )
)

;; Expand the quadtree for all children within specified bounds to specified depth.
(defn expand-quadtree [quadtree bounds depth]
  (if (>= depth 1)
    quadtree ;; Quadtree is beyond max depth; do not expand it or its children.
    (if (geometry/is-intersecting-rect bounds (:bounds quadtree))
      (let
        [
          lo (:lo (:bounds quadtree))
          hi (:hi (:bounds quadtree))
          mid (geometry/mid-point lo hi)
          bounds00 (geometry/make-rect (:x lo) (:y lo) (:x mid) (:y mid))
          bounds01 (geometry/make-rect (:x lo) (:y mid) (:x mid) (:y hi))
          bounds10 (geometry/make-rect (:x mid) (:y lo) (:x hi) (:y mid))
          bounds11 (geometry/make-rect (:x mid) (:y mid) (:x hi) (:y hi))
          child00 (valid-quadtree (:child00 quadtree) bounds00)
          child01 (valid-quadtree (:child01 quadtree) bounds01)
          child10 (valid-quadtree (:child10 quadtree) bounds10)
          child11 (valid-quadtree (:child11 quadtree) bounds11)
          expanded-child00 (expand-quadtree child00 bounds (* depth 2))
          expanded-child01 (expand-quadtree child01 bounds (* depth 2))
          expanded-child10 (expand-quadtree child10 bounds (* depth 2))
          expanded-child11 (expand-quadtree child11 bounds (* depth 2))
        ]
        ;; Expand quadtree's children to specified depth.
        (Quadtree. (:bounds quadtree) expanded-child00 expanded-child01 expanded-child10 expanded-child11 (:data quadtree))
      )
      (do
        quadtree ;; Quadtree is outside specified bounds; do not expand it or its children.
      )
    )
  )
)

(defn private-filter-quadtree [quadtree bounds depth results]
  (if (>= depth 1)
    (if (geometry/is-empty-rect (geometry/intersect-rect bounds (:bounds quadtree)))
      results
      (cons quadtree results)
    )
    (do
      (let
        [
          twice-depth (* depth 2)
          results0 (private-filter-quadtree (:child00 quadtree) bounds twice-depth results)
          results1 (private-filter-quadtree (:child01 quadtree) bounds twice-depth results0)
          results2 (private-filter-quadtree (:child10 quadtree) bounds twice-depth results1)
          results3 (private-filter-quadtree (:child11 quadtree) bounds twice-depth results2)
        ]
        results3
      )
    )
  )
)

(defn filter-quadtree [quadtree bounds depth]
  (private-filter-quadtree quadtree bounds depth '())
)

(defn private-insert-quadtree [quadtree data x y z x-mid y-mid scale]
  ;; (println " x:" x " y: " y " z: " z " x-mid: " x-mid " y-mid: " y-mid " scale: " scale) ;; DEBUG
  (let
    [
      bounds (geometry/make-rect (- x-mid scale) (- y-mid scale) (+ x-mid scale) (+ y-mid scale))
      valid-quadtree
        (if (nil? quadtree)
          (Quadtree. bounds nil nil nil nil nil)
          quadtree)
      quadtree-data (:data valid-quadtree)
      half-scale (* 0.5 scale)
    ]
    ;; (println "bounds: " bounds) ;; DEBUG
    (if (>= z 1)
      (Quadtree. bounds (:chil00 valid-quadtree) (:child01 valid-quadtree) (:child10 valid-quadtree) (:child11 valid-quadtree) data)
      (if (< x 0.0)
        (if (< y 0.0)
          ;; Third quadrant: x < 0 and y < 0 ...
          (Quadtree.
            (:bounds valid-quadtree)
            (private-insert-quadtree (:child00 valid-quadtree) data (+ (* x 2) 1) (+ (* y 2) 1) (* z 2) (- x-mid half-scale) (- y-mid half-scale) half-scale)
            (:child01 valid-quadtree)
            (:child10 valid-quadtree)
            (:child11 valid-quadtree)
            quadtree-data
          )
          ;; Second quadrant: x < 0 and y >= 0 ...
          (Quadtree.
            (:bounds valid-quadtree)
            (:child00 valid-quadtree)
            (private-insert-quadtree (:child01 valid-quadtree) data (- (* x 2) 1) (+ (* y 2) 1) (* z 2) (- x-mid half-scale) (+ y-mid half-scale) half-scale)
            (:child10 valid-quadtree)
            (:child11 valid-quadtree)
            quadtree-data
          )
        )
        (if (< y 0.0)
          ;; Fourth quadrant: x >= 0 and y < 0 ...
          (Quadtree.
            (:bounds valid-quadtree)
            (:child00 valid-quadtree)
            (:child01 valid-quadtree)
            (private-insert-quadtree (:child10 valid-quadtree) data (+ (* x 2) 1) (- (* y 2) 1) (* z 2) (+ x-mid half-scale) (- y-mid half-scale) half-scale)
            (:child11 valid-quadtree)
            quadtree-data
          )
          ;; First quadrant: x >= 0 and y >= 0 ...
          (Quadtree.
            (:bounds valid-quadtree)
            (:child00 valid-quadtree)
            (:child01 valid-quadtree)
            (:child10 valid-quadtree)
            (private-insert-quadtree (:child11 valid-quadtree) data (- (* x 2) 1) (- (* y 2) 1) (* z 2) (+ x-mid half-scale) (+ y-mid half-scale) half-scale)
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
          (Quadtree. (geometry/make-rect (- x scale) (- y scale) (+ x scale) (+ y scale)) nil nil nil nil nil)
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
