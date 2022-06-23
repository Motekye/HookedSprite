# HookedSprite

Hooked sprite-sheet algorithm for Android Java.

The Sprite object creates a sprite sheet from a single bitmap using hook pixels to define a sprite's relative x,y coordinates. You may already be familiar with the use of a special transparency color value for sprites. This method uses 3 special colors: hook, frame and transparency.

The Sprite object supports two different types of sprite sheets:

- single hook sheets for mirrored left and right facing sprites.
- four hook sheets for 2d overhead sprites.

## Single hook sheets...

![Sample of single hook sprite sheet](https://raw.githubusercontent.com/Motekye/HookedSprite/main/docu/hooked_1.png)



The algorithm searches the image from top to bottom, left to right, searching for pixels of hook
