package com.onethousandtwopixels.includes.graphics;

import android.graphics.Bitmap;
import android.graphics.Rect;


// #################################################################################################


public class Sprite {


    // ......................................................................................



    public int frame, trans;                    // frame and transparency color
    public int lhook, rhook, uhook, dhook;      // directional hook colors
    public int dirs = 0;                        // hook mode:  1,  2,  4
    public int count = 0;                       // total cells of all directions
    public int ends = 0;                        // ending y position of sprites


    // optional @Override this method, do extra work, return false if cell is invalid:
    public boolean find(int x,int y,Cell fr){ return true; }


    // ......................................................................................


    //                                                             P A L E T T E


    public int[] pal;                       // standard palette for drawing or null
    
    public int colorIndex(int c){           // get index of color or 0:
        for(int i=0;i<pal.length;i++){ if(pal[i]==c){ return i; } } return 0; }
        
                                            // make a copy of the palette, change colors?
    public int[] copyPal(){ int[] p = new int[pal.length];
        for(int i=0;i<pal.length;i++){ p[i] = pal[i]; } return p; }

    public int[] copyPal(int[] rp){ int[] p = new int[pal.length];
        for(int i=0;i<rp.length&&i<pal.length;i++){ p[i] = rp[i]; }
        for(int i=0;i<pal.length;i++){ p[i] = pal[i]; } return p; }


    // ......................................................................................



    /* *********************************************************************************************
       *********************************************************************************************


               HOOKED SPRITE SHEET ALGORITHM (c) 2015-2022, Motekye :: 1002px.com


               Takes an image from Bitmap and flips + interprets it as an
               an array of left and right facing frames with binary blit...

    */

    public Sprite(Bitmap src){

        int sh = src.getHeight(), sw = src.getWidth();
        if(sh<4||sw<5){ return; }
        int i, j, c, hk, hk2, ix, iy, jx, jy, kx, ky, lx, ly, l;
        Cell[] fr, fa; Cell f;

        // --------------------------------------------------------

        // check configuration...                                      (invalid hooks)
        ix = src.getPixel(0,0); iy = src.getPixel(1,0); if(ix==iy){ return; }
        jx = src.getPixel(0,1); jy = src.getPixel(1,1); if(jx==jy||jx==ix){ return; }
        kx = src.getPixel(1,2); ky = src.getPixel(2,1);
        lx = src.getPixel(0,4); ly = src.getPixel(1,1);

        // at least 4 directional hooks:
        if(kx!=ky&&iy!=ky&&iy!=kx&&jx!=kx&&jx!=ky&&jx!=iy // -:- not same as each-other
         &&lx!=kx&&lx!=ky&&lx!=iy&&lx!=jx // not same as 0,3 (frame)
         &&ly!=kx&&ly!=ky&&ly!=iy&&ly!=jx&&ly!=lx){ // not same as 1,1 (trans)
            frame = lx; trans = ly; dirs = 4;
            rhook = ky; lhook = jx; uhook = iy; dhook = kx;
            // TODO: test for 8 directional hooks
        }

        // only left and right hooks:
        else if(ix!=jy){ frame = ix; trans = iy; dirs = 2; rhook = ly; lhook = jx; }

        // one hook only (right), mirror sprites for left facing:
        else { frame = ix; trans = iy; dirs = 1; rhook = jx; }

        // --------------------------------------------------------

        // if trans color is a transparent pixel, then no palette is defined:
        if (trans == 0x00000000) {
            pal = null;
        // otherwise get default color palette:
        } else {
            for(i=3;i<sw;i++){ if(src.getPixel(i,0)==frame){ break; } }
            pal = new int[i-2]; pal[0] = 0x00000000;
            for(i=1;i<pal.length;i++){ pal[i]=src.getPixel(i+2,0); }
        }


        // --------------------------------------------------------

        // search for pixels of hook until first column no longer frame...
        if(dirs<4){ iy=2; } else { iy=3; } // < start below hook definition
        for(; iy<sh; iy++){ if(src.getPixel(0,iy)!=frame){ break; }
        for(ix=1; ix<sw; ix++){ c=src.getPixel(ix,iy);
        if(c==lhook||c==rhook||c==uhook||c==dhook){ hk = c;

            // ................................

            // hook below or inside, find top:
            if(src.getPixel(ix,iy-1)!=frame){
                for(jy=iy; jy>0; jy--){ if(src.getPixel(ix,jy-1)==frame){ break; } } }

            // hook at the top of the sprite or lone hook:
            else { jy=iy; }

            // find top left corner:
            for(jx=ix-1; jx>=1; jx--){ c=src.getPixel(jx,jy); if(c==frame){ break; } } jx++;

            // find width of sprite across top:
            for(kx=jx; kx<sw; kx++){ if(src.getPixel(kx,jy)==frame){ break; } } kx--;

            // find height of sprite (top-right to bottom-right):
            for(ky=jy; ky<sh; ky++){ if(src.getPixel(kx,ky)==frame){ break; } } ky--;

            // adjust for hooks appearing at the far right: (and distension)
            if(jx!=kx){ while(src.getPixel(kx-1,ky)==frame){ ky--; } }

            // ................................

            // choose directional array, enlarge:
            if(hk==lhook){ fa = Lf; l = fa.length; }
            else if(hk==rhook){ fa = Rf; l = fa.length; }
            else if(hk==uhook){ fa = Uf; l = fa.length; }
            else if(hk==dhook){ fa = Df; l = fa.length; }
            else { continue; }

            fr = new Cell[l+1]; for(i=0;i<l;i++){ fr[i]=fa[i]; }

            // ................................

            // create new sprite frame, calculate offset:
            f = new Cell(kx-jx+1,ky-jy+1); fr[i]=f; f.x=jx-ix; f.y=jy-iy;

            // store colors directly in matrix:
            if (pal==null){
                for(j=-1,ly=0;ly<fr[i].h;ly++){ for(lx=0;lx<f.w;lx++){ j++;
                    c = src.getPixel(jx+lx,jy+ly);
                    // transparent or color:
                    if(c==trans||c==frame||c==hk){ f.blt[j]=false; f.spr[j]=0x00000000; }
                    else { fr[i].blt[j]=true; f.spr[j]=c; }
                } }

            // store colors by palette index:
            } else {
                for(j=-1,ly=0;ly<fr[i].h;ly++){ for(lx=0;lx<f.w;lx++){ j++;
                    c = src.getPixel(jx+lx,jy+ly);
                    // transparent or color by index:
                    if(c==trans||c==frame||c==hk){ f.blt[j]=false; f.spr[j]=0; }
                    else { fr[i].blt[j]=true; f.spr[j]=colorIndex(c); }
                } }
            }

            // ................................

            // proceed by result of additional find event: (for extending classes)
            if(!find(ix,iy,f)){ continue; }

            // ................................
            
            // put sprite in appropriate array, update total count:
            if(hk==lhook){ Lf = fr; }
            else if(hk==rhook){ Rf = fr; }
            else if(hk==uhook){ Uf = fr; }
            else if(hk==dhook){ Df = fr; }
            count++;

            // ................................

            // with only one hook defined, left facing sprites are just mirrored copies:
            if(dirs==1){
                fr = new Cell[Lf.length+1];
                for(i=0;i<Lf.length;i++){ fr[i]=Lf[i]; }
                Lf = fr; Lf[i] = Rf[i].mirrored();
            }
            
            // ................................
                
            // check for additional hooks indicating mirrored or rotated copies:
            else {
                c=src.getPixel(ix+1,iy);
                if(c==lhook||c==rhook||c==uhook||c==dhook){ hk2=c; }
                else { continue; }
                if(hk==hk2){ continue; }

                // mirror left / right hooks:
                if(hk==rhook&&hk2==lhook){
                    fr = new Cell[Lf.length+1];
                    for(i=0;i<Lf.length;i++){ fr[i]=Lf[i]; }
                    Lf = fr; Lf[i] = Rf[i].mirrored();
                    count++;
                }
                if(hk==lhook&&hk2==rhook){
                    fr = new Cell[Rf.length+1];
                    for(i=0;i<Rf.length;i++){ fr[i]=Rf[i]; }
                    Rf = fr; Rf[i] = Lf[i].mirrored();
                    count++;
                }

                // TODO: flip up / down hooks:

            }

            // ................................

        } } }
        ends = iy;

        // --------------------------------------------------------
    }

    /* *********************************************************************************************
       *********************************************************************************************
    */

    // ......................................................................................


    public class Cell {         //                               E A C H   F R A M E


        public int w;               // width of frame
        public int h;               // height of frame
        public int l;               // pixel count of rectangle (w*h)
        public int x=0;             // offset x pos from hook
        public int y=0;             // offset y pos from hook
        public boolean[] blt;       // binary blit map
        public int[] spr;           // sprite pixels as indexes in pal[]

        // create an empty cell:
        public Cell(){ l=1; w=1; h=1; blt=new boolean[1]; spr=new int[1]; blt[0]=false; spr[0]=0; }

        // create a new cell:
        public Cell(int aw,int ah){ l=aw*ah; w=aw; h=ah; blt=new boolean[l]; spr=new int[l]; }

        // return new mirrored copy of cell:
        public Cell mirrored(){ Cell o = new Cell(w,h); o.y=y; o.x=-1*(w+x-1);
            for(int ix,iy=0,i,f;iy<h;iy++){ i=iy*w; f=((iy+1)*w)-1;
            for(ix=0;ix<w;ix++,i++,f--){ o.blt[i]=blt[f]; o.spr[i]=spr[f]; } } return o; }

        // TODO: create new cell as flipped copy:

        // TODO: create new cell as rotated copy:

        // TODO: count colors

        // TODO: add another cell, (at?) expanding, (shifted palette?)

    }

    public Cell[] Lf = new Cell[0];
    public Cell[] Rf = new Cell[0];
    public Cell[] Uf = new Cell[0];
    public Cell[] Df = new Cell[0];

    // get a specific cell from arrays by direction:
    public Cell cell(int fr,int dr){ switch(dirs){
        case 1: case 2: if(dr==1){ return Rf[fr]; } return Lf[fr];
        case 4: switch(dr){
            case 0: return Uf[fr]; case 1: return Rf[fr];
            case 2: return Df[fr]; case 3: return Lf[fr];
    } } return null; }

    // get relative height and width of cells:
    public int heightOf(int fr, int dr){ return cell(fr,dr).y * -1; }
    public int widthOf(int fr, int dr){ return cell(fr,dr).w; }


    // ......................................................................................





    // ######################################################################################
    // ......................................................................................


    //                                                              C L I P P I N G


    // ......................................................................................


    // determine if sprite is within top, right, bottom, left:
    public boolean within(int t,int r,int b,int l,int fr,int dr,int ax,int ay){
        Cell f=cell(fr,dr); if(f==null){ return false; }
        return (ax+f.x<r&&ax+f.x+f.w>l&&ay+f.y<b&&ay+f.y+f.h>t); }

    // determine if sprite is within int[] top, right, bottom, left:
    public boolean within(int[] b,int fr,int dr,int ax,int ay){
        Cell f=cell(fr,dr); if(f==null){ return false; }
        return (ax+f.x<b[1]&&ax+f.x+f.w>b[3]&&ay+f.y<b[2]&&ay+f.y+f.h>b[0]); }

    // determine if sprite is within Rect object:
    public boolean within(Rect r, int fr, int dr, int ax, int ay){
        Cell f=cell(fr,dr); if(f==null){ return false; }
        return (ax+f.x<r.right&&ax+f.x+f.w>r.left&&ay+f.y<r.bottom&&ay+f.y+f.h>r.top); }


    // ......................................................................................


    // get maximum of two [T,R,B,L] sprite boxes:
    public static int[] max(int[] a, int[] b){
        if(a[0]==-1){ return b; }
        if(b[0]==-1){ return a; } int[] r = new int[4];
        if(a[0]<b[0]){ r[0]=a[0]; } else { r[0]=b[0]; } // top
        if(a[1]>b[1]){ r[1]=a[1]; } else { r[1]=b[1]; } // right
        if(a[2]>b[2]){ r[2]=a[2]; } else { r[2]=b[2]; } // bottom
        if(a[3]<b[3]){ r[3]=a[3]; } else { r[3]=b[3]; } // left
        return r; }


    // ......................................................................................


    // pixel perfect collision relative to hook position:
    // (check that position relative to cell is not transparent)
    public boolean clip(int fr,int dr,int ax,int ay){
        Cell f=cell(fr,dr); if(f==null){ return false; } int sx, sy;
        sx=ax-f.x; sy=ay-f.y; if(sx<0||sy<0||sx>f.w-1||sy>f.h-1){ return false; }
        return f.blt[(sy*f.w)+sx]; }


    // ......................................................................................





    // ######################################################################################
    // ......................................................................................


    //                                                              D R A W I N G


    // ......................................................................................


    //                                                          DRAW ONTO


    // draw sprite onto a bitmap:
    public int[] drawOnto(Bitmap s,int fr,int dr,int ax,int ay){
        return drawOnto(s,fr,dr,ax,ay,pal); }

    // draw onto using custom palette:
    public int[] drawOnto(Bitmap s,int fr,int dr,int ax,int ay,int[] pl){
        int ix,iy,i=-1,zx=0,zy=0,dx,dy,nx,ny,oh,ow,mw=s.getWidth(),mh=s.getHeight();
        Cell sp = cell(fr,dr);
        dx=ax+sp.x; dy=ay+sp.y; ow=sp.w; oh=sp.h;
        for(iy=zy;iy<oh;iy++){ ny=dy+iy; if(ny<0){ i+=ow; continue; } if(ny>=mh){ break; }
        for(ix=zx;ix<ow;ix++){ nx=dx+ix; if(nx<0){ i++; continue; } if(nx>=mw){ i+=ow-ix; break; }
            i++; if(!sp.blt[i]){ continue; }
            s.setPixel(dx+ix,dy+iy, (pl==null?sp.spr[i]:pl[sp.spr[i]]) );
        } }
        return new int[]{ dy, dx+ow, dy+oh, dx };
    }

    // draw onto using solid color:
    public int[] drawOnto(Bitmap s,int fr,int dr,int ax,int ay,int cl){
        int ix,iy,i=-1,zx=0,zy=0,dx,dy,nx,ny,oh,ow,mw=s.getWidth(),mh=s.getHeight();
        Cell sp = cell(fr,dr);
        dx=ax+sp.x; dy=ay+sp.y; ow=sp.w; oh=sp.h;
        for(iy=zy;iy<oh;iy++){ ny=dy+iy; if(ny<0){ i+=ow; continue; } if(ny>=mh){ break; }
        for(ix=zx;ix<ow;ix++){ nx=dx+ix; if(nx<0){ i++; continue; } if(nx>=mw){ i+=ow-ix; break; }
            i++; if(!sp.blt[i]){ continue; }
            s.setPixel(dx+ix,dy+iy,cl);
        } }
        return new int[]{ dy, dx+ow, dy+oh, dx };
    }


    // ......................................................................................


    //                                                          CLEAR FROM


    // clear sprite from source layer using revert layer:
    public int[] clearFrom(Bitmap s,int fr,int dr,int ax,int ay,Bitmap rv){
        int ix,iy,i=-1,zx=0,zy=0,dx,dy,nx,ny,oh,ow,mw=s.getWidth(),mh=s.getHeight();
        Cell sp = cell(fr,dr);
        dx=ax+sp.x; dy=ay+sp.y; ow=sp.w; oh=sp.h;
        for(iy=zy;iy<oh;iy++){ ny=dy+iy; if(ny<0){ i+=ow; continue; } if(ny>=mh){ break; }
        for(ix=zx;ix<ow;ix++){ nx=dx+ix; if(nx<0){ i++; continue; } if(nx>=mw){ i+=ow-ix; break; }
            i++; if(!sp.blt[i]){ continue; }
            s.setPixel(dx+ix,dy+iy, rv.getPixel(dx+ix,dy+iy) );
        } }
        return new int[]{ dy, dx+ow, dy+oh, dx };
    }


    // ......................................................................................


}