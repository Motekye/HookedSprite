# HookedSprite

Hooked sprite-sheet algorithm for Android Java.

The Sprite object creates a sprite sheet from a single bitmap using hook pixels to define a sprite's relative x,y coordinates.

You may be familiar with the use of a special transparency color value for sprites. This method uses 3 special colors: hook, frame and transparency.



The algorithm searches the image from top to bottom, left to right, searching for pixels of hook
