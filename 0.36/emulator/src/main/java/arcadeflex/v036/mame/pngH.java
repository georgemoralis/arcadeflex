/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

public class pngH {

    /*TODO*///
/*TODO*///#ifndef MAME_PNG_H
/*TODO*///#define MAME_PNG_H
/*TODO*///
/*TODO*///
/*TODO*///#define PNG_Signature       "\x89\x50\x4E\x47\x0D\x0A\x1A\x0A"
/*TODO*///
/*TODO*///#define PNG_CN_IHDR 0x49484452L     /* Chunk names */
/*TODO*///#define PNG_CN_PLTE 0x504C5445L
/*TODO*///#define PNG_CN_IDAT 0x49444154L
/*TODO*///#define PNG_CN_IEND 0x49454E44L
/*TODO*///#define PNG_CN_gAMA 0x67414D41L
/*TODO*///#define PNG_CN_sBIT 0x73424954L
/*TODO*///#define PNG_CN_cHRM 0x6348524DL
/*TODO*///#define PNG_CN_tRNS 0x74524E53L
/*TODO*///#define PNG_CN_bKGD 0x624B4744L
/*TODO*///#define PNG_CN_hIST 0x68495354L
/*TODO*///#define PNG_CN_tEXt 0x74455874L
/*TODO*///#define PNG_CN_zTXt 0x7A545874L
/*TODO*///#define PNG_CN_pHYs 0x70485973L
/*TODO*///#define PNG_CN_oFFs 0x6F464673L
/*TODO*///#define PNG_CN_tIME 0x74494D45L
/*TODO*///#define PNG_CN_sCAL 0x7343414CL
/*TODO*///
/*TODO*///#define PNG_PF_None     0   /* Prediction filters */
/*TODO*///#define PNG_PF_Sub      1
/*TODO*///#define PNG_PF_Up       2
/*TODO*///#define PNG_PF_Average  3
/*TODO*///#define PNG_PF_Paeth    4
/*TODO*///
/* PNG support */
    public static class png_info {

        int/*UINT32*/ width, height;
        /*TODO*///	UINT32 xoffset, yoffset;
/*TODO*///	UINT32 xres, yres;
        double xscale, yscale;
        double source_gamma;
        /*TODO*///	UINT32 chromaticities[8];
/*TODO*///	UINT32 resolution_unit, offset_unit, scale_unit;
/*TODO*///	UINT8 bit_depth;
/*TODO*///	UINT32 significant_bits[4];
/*TODO*///	UINT32 background_color[4];
/*TODO*///	UINT8 color_type;
/*TODO*///	UINT8 compression_method;
/*TODO*///	UINT8 filter_method;
/*TODO*///	UINT8 interlace_method;
        int /*UINT32*/ num_palette;
        char[] /*UINT8 * */ palette;
        int /*UINT32*/ num_trans;
        char[] /*UINT8 * */ trans;
        char[] /*UINT8 * */ image;
        /*TODO*///
/*TODO*///	/* The rest is private and should not be used
/*TODO*///	 * by the public functions
/*TODO*///	 */
/*TODO*///	UINT8 bpp;
/*TODO*///	UINT32 rowbytes;
/*TODO*///	UINT8 *zimage;
/*TODO*///	UINT32 zlength;
/*TODO*///	UINT8 *fimage;
    }
    /*TODO*///
/*TODO*///int png_verify_signature (void *fp);
/*TODO*///int png_inflate_image (struct png_info *p);
/*TODO*///int png_read_file(void *fp, struct png_info *p);
/*TODO*///int png_expand_buffer_8bit (struct png_info *p);
/*TODO*///void png_delete_unused_colors (struct png_info *p);
/*TODO*///int png_unfilter(struct png_info *p);
/*TODO*///
/*TODO*///int png_write_bitmap(void *fp, struct osd_bitmap *bitmap);
/*TODO*///
/*TODO*///#endif
/*TODO*///
/*TODO*///    
}
