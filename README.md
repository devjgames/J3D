# J3D
Java 3D Game Engine

Builds with maven

Depends on LWJGL, GLFW, OpenGL, OpenAL & JOML

# Features

Game - manages game window and input

IO - utilities for file handling

Utils - utilities for working with OpenGL state and graphics

AssetManager - extendable asset loading system

Texture - manage OpenGL textures

RenderTarget - manage OpenGL RenderTargets

Pipeline - manage OpenGL pipelines

SpritePipeline - render text and sprites

Font - manages a row/column fixed width bitmap font

Sound - plays small wav sounds

UIManager - create and run a UI in the same place with built-in label, text field, slider & list controls

Resource - base class for all classes that need to be managed by a resource manager

Mesh - configurable mesh class

MeshLoader - loads position/texture/normal meshes from OBJ files

Collider - class for resolving player movement against triangles in a scene

LightMapper - light mapping system, works best for quad primitives (aligned to 16 unit texels) with no hidden faces

