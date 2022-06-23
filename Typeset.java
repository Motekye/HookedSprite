//package insert.package.name;

import android.graphics.Bitmap;


// #################################################################################################


public class Typeset extends Sprite {

    public Typeset(Bitmap b){ super(b); }


    // .............................................................................


    // draw a letter onto a bitmap:
    public void chr(Bitmap d,int l,int x,int y,int c){
        if(l<32||l>127){ l=127; } drawOnto(d,l-32,1,x,y,c); }
    public void chr(Bitmap d,char h,int x,int y,int c){ int l=(int) h;
        if(l<32||l>127){ l=127; } drawOnto(d,l-32,1,x,y,c); }
    public void chr(Bitmap d,String s,int p,int x,int y,int c){ int l=(int) s.charAt(p);
        if(l<32||l>127){ l=127; } drawOnto(d,l-32,1,x,y,c); }

    // get metrics of one letter:
    public Cell character(int l){
        if(l<32||l>127){ l=127; } l-=32; return Rf[l]; }
    public Cell character(char h){ int l=(int) h;
        if(l<32||l>127){ l=127; } l-=32; return Rf[l]; }
    public int widthOf(int l){
        if(l<32||l>127){ l=127; } l-=32; return Rf[l].w; }
    public int widthOf(char h){ int l=(int) h;
        if(l<32||l>127){ l=127; } l-=32; return Rf[l].w; }


    // .............................................................................


    // write a single line of text:
    public void line(Bitmap d,String s,int x,int y,int c,int k){
        int j, i; char z;
        for(j=x,i=0;i<s.length();i++){
            z = s.charAt(i);
            chr(d,z,j,y,c);
            j += widthOf(z) + k;
        }
    }

    // write a single line of text: (right justified)
    public void line_right(Bitmap d,String s,int x,int y,int c,int k){
        int j=x, i; char z;
        for(i=s.length()-1;i>-1;i--){
            z = s.charAt(i);
            j -= widthOf(z) + k;
            chr(d,z,j,y,c);
        }
    }

    // write a single line of text: (centered)
    public void line_center(Bitmap d,String s,int x,int y,int c,int k){
        int j, i, wd=0; char z;
        for(i=0;i<s.length();i++){ z = s.charAt(i); wd+=widthOf(z) + k; }
        j = x - ((wd-k)/2);
        for(i=0;i<s.length();i++){ z = s.charAt(i); chr(d,z,j,y,c); j += widthOf(z) + k; }
    }


    // .............................................................................


    // write a word-wrapping paragraph of typeset onto a bitmap:
    public void para(Bitmap d,String s,int x,int y,int c,int k,int lh,int w){
        int i, ix=x, iy=y, l=s.length(), n=0, j; char z; String m="";
        for(i=0;i<l;i++){ z = s.charAt(i);

            // new lines:
            if(z=='\n'||z=='\r'){
                if(z=='\r'&&i<l-1&&s.charAt(i+1)=='\n'){ i++; }
                ix=x; iy+=lh; }

            // todo: tab character
            else if(z=='\t'){  }

            // add character to word:
            else { n+=widthOf(z)+k; m+=z; }

            // word-breaking characters?
            if(z<48||(z>57&&z<65)||(z>90&&z<97)||(z>122&&z<127)){

                // draw word:
                if(ix+n-k>x+w){ ix=x; iy+=lh; } // too long?
                for(j=0;j<m.length();j++){
                    chr(d,m.charAt(j),ix,iy,c); ix+=widthOf(m.charAt(j))+k;
                } n=0; m="";
            }
        }
        // draw final word:
        if(ix+n-k>x+w){ ix=x; iy+=lh; } // too long?
        for(j=0;j<m.length();j++){
            chr(d,m.charAt(j),ix,iy,c); ix+=widthOf(m.charAt(j))+k;
        }

    }


    // .............................................................................


}
