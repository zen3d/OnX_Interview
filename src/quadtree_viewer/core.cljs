(ns quadtree-viewer.core
  (:require
    [quadtree-viewer.quadtree :as quadtree]
    [quadtree-viewer.mouse-tracker :as mouse-tracker]
    [quadtree-viewer.geometry :as geometry]
  )
)

(enable-console-print!)
(println "starting threejs-cljs")

(def root (quadtree/make-quadtree))
(set! root (quadtree/insert-quadtree root nil 0.33 0.33 0.15))
(def test-quadtree (quadtree/seek-quadtree root 0.33 0.33 0.15))
(println (:bounds test-quadtree))
(def test-segment (geometry/make-rect-lines (:bounds test-quadtree)))
(println test-segment)

(defn private-geometry-from-segments [segments geometry]
  (loop
    [
      segs segments
      geo []
    ]
    (if (empty? segs)
      (clj->js geo)
      (let
        [
          start (nth segs 0)
          stop (nth segs 1)
          vec3-start (js/THREE.Vector3. (* 100 (nth start 0)) (* 100 (nth start 1)) 0)
          vec3-stop (js/THREE.Vector3. (* 100 (nth stop 0)) (* 100 (nth stop 1)) 0)
          start-geo (conj geo vec3-start)
          stop-geo (conj start-geo vec3-stop)
        ]
        ;;(println "start: " (. vec3-start -x) (. vec3-start -y) (. vec3-start -z))
        ;;(println "stop: " vec3-stop)
        (recur (subvec segs 2) stop-geo)
      )
    )
  )
)

(defn geometry-from-segments [segments]
  (private-geometry-from-segments segments (make-array 0))
)

(def geo (geometry-from-segments test-segment))
(println geo)

(def geo3 (js/THREE.Geometry. geo))
(println geo3)

(defn make-square []
  (let
    [
      geo (js/THREE.Geometry.)
      verts (. geo -vertices)
      faces (. geo -faces)
      r 0.995 ;; inset square edge a little bit
    ]
    (. verts push (js/THREE.Vector3. (- r) (- r) 0))
    (. verts push (js/THREE.Vector3. r (- r)  0))
    (. verts push (js/THREE.Vector3. r r 0))
    (. verts push (js/THREE.Vector3. (- r) r 0))
    (. verts push (js/THREE.Vector3. 0 0 0))

    (. faces push (js/THREE.Face3. 0 1 4))
    (. faces push (js/THREE.Face3. 1 2 4))
    (. faces push (js/THREE.Face3. 2 3 4))
    (. faces push (js/THREE.Face3. 3 0 4))

    geo
  )
)

;; Reset root after tests.
(set! root (quadtree/make-quadtree))

(def x-last 0)
(def y-last 0)
(def z-last 0)

(defn add-group-geometries [group geometries]
  (println "group: " group)
  (if (empty? geometries)
    group
    (add-group-geometries (.add group (first geometries)) (rest geometries))
  )
)

(defn private-make-squares [num result]
  (if (> num 0)
    (let
      [
        square-geometry (make-square) ;; square-geo ;;  (js/THREE.CubeGeometry. 1 1 1)
        square-wireframe (js/THREE.WireframeGeometry. square-geometry)
        ;; material (js/THREE.LineBasicMaterial. );;(js/THREE.MeshBasicMaterial. (clj->js {:color 0x00ff00}))
        square-mesh (js/THREE.LineSegments. square-wireframe);;(js/THREE.Mesh. geometry material)
      ]
      (private-make-squares (- num 1) (cons square-mesh result))
    )
    result
  )
)

(defn make-squares [num]
  (private-make-squares num '())
)

(def group-squares (add-group-geometries (js/THREE.Group.) (make-squares 9)))

(defn render-candidate [candidate geometry]
  (if (nil? candidate)
    (set! (.. geometry -visible) js/false)
    (let
      [
        bounds (:bounds candidate)
        lo (:lo bounds)
        hi (:hi bounds)
        mid (geometry/mid-point lo hi)
        x-mid (:x mid)
        y-mid (:y mid)
        x-scale (* 0.5 (- (:x hi) (:x lo)))
        y-scale (* 0.5 (- (:y hi) (:y lo)))
      ]
      (set! (.. geometry -visible) js/true)
      (set! (.. geometry -position -x) x-mid)
      (set! (.. geometry -position -y) y-mid)
      (set! (.. geometry -scale -x) x-scale)
      (set! (.. geometry -scale -y) y-scale)
    )
  )
)

(defn private-render-candidates [candidates group-children idx group-length]
  (if (< idx group-length)
    (do
      (render-candidate (first candidates) (nth group-children idx))
      (private-render-candidates (rest candidates) group-children (+ idx 1) group-length)
    )
  )
)

(defn render-candidates [candidates]
  (let
    [
      group-children (.. group-squares -children)
      group-length (.. group-children -length)
    ]
    (private-render-candidates candidates group-children 0 group-length)
  )
  ;;(if (not (nil? candidates))
  ;;  (render-candidate (first candidates) group)
  ;;  (render-candidates (rest candidates) group)
  ;;)
)

(defn ^:export quadtree-viewer []
  (let
    [
      scene (js/THREE.Scene.)
      width (.-innerWidth js/window)
      height (.-innerHeight js/window)
      aspect-ratio (/ width height)
      ;;camera (js/THREE.OrthographicCamera. (* -1 aspect-ratio) (* 1 aspect-ratio) -1 1 -0.001 10.0)
      camera (js/THREE.PerspectiveCamera. 91 aspect-ratio 0.00001 2 )
      renderer (js/THREE.WebGLRenderer.)
      geometry (make-square) ;; square-geo ;;  (js/THREE.CubeGeometry. 1 1 1)
      wireframe (js/THREE.WireframeGeometry. geometry)
      material (js/THREE.LineBasicMaterial. );;(js/THREE.MeshBasicMaterial. (clj->js {:color 0x00ff00}))
      cube (js/THREE.LineSegments. wireframe);;(js/THREE.Mesh. geometry material)
      render (fn cb []
        (js/requestAnimationFrame cb)
        (let
          [
            x (.. camera -position -x)
            y (.. camera -position -y)
            z (.. camera -position -z)
          ]

          ;; Don't traverse quadtree if camera has not moved.
          (if (or (not (== x x-last)) (not (== y y-last)) (not (== z z-last)))
            (do
              (pr "current cursor: " x y z) ;; DEBUG
              (set! root (quadtree/expand-quadtree root (geometry/make-rect (- x z) (- y z) (+ x z) (+ y z)) z))
              (let
                [
                  size (geometry/Point. z z)
                  center (geometry/Point. x y)
                  bounds (geometry/Rect. (geometry/sub-point center size) (geometry/add-point center size))
                  candidates (quadtree/filter-quadtree root bounds z)
                  leaf (quadtree/seek-quadtree root x y z)
                  bounds (:bounds leaf)
                  x-lo (:x (:lo bounds))
                  y-lo (:y (:lo bounds))
                  x-hi (:x (:hi bounds))
                  y-hi (:y (:hi bounds))
                  x-mid (* 0.5 (+ x-hi x-lo))
                  y-mid (* 0.5 (+ y-hi y-lo))
                  x-scale (* 0.5 (- x-hi x-lo))
                  y-scale (* 0.5 (- y-hi y-lo))
                ]
                (println "Number of candidates: " (count candidates))
                ;;(set! (.. cube -position -x) x-mid)
                ;;(set! (.. cube -position -y) (- y-mid))
                ;;(set! (.. cube -scale -x) x-scale)
                ;;(set! (.. cube -scale -y) y-scale)
                (render-candidates candidates)
              )
            )
          )

          (.render renderer scene camera)

          (set! x-last x)
          (set! y-last y)
          (set! z-last z)
        )
      )
    ]
    (mouse-tracker/track-camera camera js/document)
    (.setSize renderer width height)
    (.appendChild js/document.body (.-domElement renderer) )
    (.add scene group-squares)
    (set! (.-z (.-position camera))  1)
    (render)
  )
)

(quadtree-viewer)
