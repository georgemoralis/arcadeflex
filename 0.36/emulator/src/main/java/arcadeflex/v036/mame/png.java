/**
 * ported to v0.36
 */
package arcadeflex.v036.mame;

//mame imports
import static arcadeflex.v036.mame.osdependH.*;

public class png {

    /*TODO*////*********************************************************************
/*TODO*///
/*TODO*///  png.c
/*TODO*///
/*TODO*///  PNG reading functions.
/*TODO*///
/*TODO*///  07/15/1998 Created by Mathis Rosenhauer
/*TODO*///  10/02/1998 Code clean up and abstraction by Mike Balfour
/*TODO*///             and Mathis Rosenhauer
/*TODO*///  10/15/1998 Image filtering. MLR
/*TODO*///  11/09/1998 Bit depths 1-8 MLR
/*TODO*///  11/10/1998 Some additional PNG chunks recognized MLR
/*TODO*///  05/14/1999 Color type 2 and PNG save functions added
/*TODO*///  05/15/1999 Handle RGB555 while saving, use osd_fxxx
/*TODO*///             functions for writing MSH
/*TODO*///
/*TODO*///  TODO : Fully comply with the "Recommendations for Decoders"
/*TODO*///         of the W3C
/*TODO*///
/*TODO*///*********************************************************************/
/*TODO*///
/*TODO*///#include <math.h>
/*TODO*///#include <zlib.h>
/*TODO*///#include "driver.h"
/*TODO*///#include "png.h"
/*TODO*///
/*TODO*///extern char build_version[];
/*TODO*///
/*TODO*////* convert_uint is here so we don't have to deal with byte-ordering issues */
/*TODO*///static UINT32 convert_from_network_order (UINT8 *v)
/*TODO*///{
/*TODO*///	UINT32 i;
/*TODO*///
/*TODO*///	i = (v[0]<<24) | (v[1]<<16) | (v[2]<<8) | (v[3]);
/*TODO*///	return i;
/*TODO*///}
/*TODO*///
/*TODO*///int png_unfilter(struct png_info *p)
/*TODO*///{
/*TODO*///	int i, j, bpp, filter;
/*TODO*///	INT32 prediction, pA, pB, pC, dA, dB, dC;
/*TODO*///	UINT8 *src, *dst;
/*TODO*///
/*TODO*///	if((p->image = (UINT8 *)malloc (p->height*p->rowbytes))==NULL)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Out of memory\n");
/*TODO*///		free (p->fimage);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	src = p->fimage;
/*TODO*///	dst = p->image;
/*TODO*///	bpp = p->bpp;
/*TODO*///
/*TODO*///	for (i=0; i<p->height; i++)
/*TODO*///	{
/*TODO*///		filter = *src++;
/*TODO*///		if (!filter)
/*TODO*///		{
/*TODO*///			memcpy (dst, src, p->rowbytes);
/*TODO*///			src += p->rowbytes;
/*TODO*///			dst += p->rowbytes;
/*TODO*///		}
/*TODO*///		else
/*TODO*///			for (j=0; j<p->rowbytes; j++)
/*TODO*///			{
/*TODO*///				pA = (j<bpp) ? 0: *(dst - bpp);
/*TODO*///				pB = (i<1) ? 0: *(dst - p->rowbytes);
/*TODO*///				pC = ((j<bpp)||(i<1)) ? 0: *(dst - p->rowbytes - bpp);
/*TODO*///
/*TODO*///				switch (filter)
/*TODO*///				{
/*TODO*///				case PNG_PF_Sub:
/*TODO*///					prediction = pA;
/*TODO*///					break;
/*TODO*///				case PNG_PF_Up:
/*TODO*///					prediction = pB;
/*TODO*///					break;
/*TODO*///				case PNG_PF_Average:
/*TODO*///					prediction = ((pA + pB) / 2);
/*TODO*///					break;
/*TODO*///				case PNG_PF_Paeth:
/*TODO*///					prediction = pA + pB - pC;
/*TODO*///					dA = abs(prediction - pA);
/*TODO*///					dB = abs(prediction - pB);
/*TODO*///					dC = abs(prediction - pC);
/*TODO*///					if (dA <= dB && dA <= dC) prediction = pA;
/*TODO*///					else if (dB <= dC) prediction = pB;
/*TODO*///					else prediction = pC;
/*TODO*///					break;
/*TODO*///				default:
/*TODO*///					if (errorlog)
/*TODO*///						fprintf(errorlog,"Unknown filter type %i\n",filter);
/*TODO*///					prediction = 0;
/*TODO*///				}
/*TODO*///				*dst = 0xff & (*src + prediction);
/*TODO*///				dst++; src++;
/*TODO*///			}
/*TODO*///	}
/*TODO*///
/*TODO*///	free (p->fimage);
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///int png_verify_signature (void *fp)
/*TODO*///{
/*TODO*///	INT8 signature[8];
/*TODO*///
/*TODO*///	if (osd_fread (fp, signature, 8) != 8)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Unable to read PNG signature (EOF)\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (memcmp(signature, PNG_Signature, 8))
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"PNG signature mismatch found: %s expected: %s\n",
/*TODO*///				signature,PNG_Signature);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///int png_inflate_image (struct png_info *p)
/*TODO*///{
/*TODO*///	unsigned long fbuff_size;
/*TODO*///
/*TODO*///	fbuff_size = p->height * (p->rowbytes + 1);
/*TODO*///
/*TODO*///	if((p->fimage = (UINT8 *)malloc (fbuff_size))==NULL)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Out of memory\n");
/*TODO*///		free (p->zimage);
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (uncompress(p->fimage, &fbuff_size, p->zimage, p->zlength) != Z_OK)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Error while inflating image\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	free (p->zimage);
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///int png_read_file(void *fp, struct png_info *p)
/*TODO*///{
/*TODO*///	/* translates color_type to bytes per pixel */
/*TODO*///	const int samples[] = {1, 0, 3, 1, 2, 0, 4};
/*TODO*///
/*TODO*///	int i;
/*TODO*///	UINT32 chunk_length, chunk_type=0, chunk_crc, crc;
/*TODO*///	UINT32 tot_idat=0, num_idat=0, l_idat[256];
/*TODO*///	UINT8 *chunk_data, *zimage, *idat[256], *temp;
/*TODO*///	UINT8 str_chunk_type[5], v[4];
/*TODO*///
/*TODO*///	p->num_palette = 0;
/*TODO*///	p->num_trans = 0;
/*TODO*///	p->trans = NULL;
/*TODO*///	p->palette = NULL;
/*TODO*///
/*TODO*///	if (png_verify_signature(fp)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	while (chunk_type != PNG_CN_IEND)
/*TODO*///	{
/*TODO*///		if (osd_fread(fp, v, 4) != 4)
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"Unexpected EOF in PNG\n");
/*TODO*///		chunk_length=convert_from_network_order(v);
/*TODO*///
/*TODO*///		if (osd_fread(fp, str_chunk_type, 4) != 4)
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"Unexpected EOF in PNG file\n");
/*TODO*///
/*TODO*///		str_chunk_type[4]=0; /* terminate string */
/*TODO*///
/*TODO*///		crc=crc32(0,str_chunk_type, 4);
/*TODO*///		chunk_type = convert_from_network_order(str_chunk_type);
/*TODO*///
/*TODO*///		if (chunk_length)
/*TODO*///		{
/*TODO*///			if ((chunk_data = (UINT8 *)malloc(chunk_length+1))==NULL)
/*TODO*///			{
/*TODO*///				if (errorlog)
/*TODO*///					fprintf(errorlog,"Out of memory\n");
/*TODO*///				return 0;
/*TODO*///			}
/*TODO*///			if (osd_fread (fp, chunk_data, chunk_length) != chunk_length)
/*TODO*///			{
/*TODO*///				if (errorlog)
/*TODO*///					fprintf(errorlog,"Unexpected EOF in PNG file\n");
/*TODO*///				free(chunk_data);
/*TODO*///				return 0;
/*TODO*///			}
/*TODO*///
/*TODO*///			crc=crc32(crc,chunk_data, chunk_length);
/*TODO*///		}
/*TODO*///		else
/*TODO*///			chunk_data = NULL;
/*TODO*///
/*TODO*///		if (osd_fread(fp, v, 4) != 4)
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"Unexpected EOF in PNG\n");
/*TODO*///		chunk_crc=convert_from_network_order(v);
/*TODO*///
/*TODO*///		if (crc != chunk_crc)
/*TODO*///		{
/*TODO*///			if (errorlog)
/*TODO*///			{
/*TODO*///				fprintf(errorlog,"CRC check failed while reading PNG chunk %s\n",
/*TODO*///					str_chunk_type);
/*TODO*///				fprintf(errorlog,"Found: %08X  Expected: %08X\n",crc,chunk_crc);
/*TODO*///			}
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Reading PNG chunk %s\n", str_chunk_type);
/*TODO*///
/*TODO*///		switch (chunk_type)
/*TODO*///		{
/*TODO*///		case PNG_CN_IHDR:
/*TODO*///			p->width = convert_from_network_order(chunk_data);
/*TODO*///			p->height = convert_from_network_order(chunk_data+4);
/*TODO*///			p->bit_depth = *(chunk_data+8);
/*TODO*///			p->color_type = *(chunk_data+9);
/*TODO*///			p->compression_method = *(chunk_data+10);
/*TODO*///			p->filter_method = *(chunk_data+11);
/*TODO*///			p->interlace_method = *(chunk_data+12);
/*TODO*///			free (chunk_data);
/*TODO*///
/*TODO*///			if (errorlog)
/*TODO*///			{
/*TODO*///				fprintf(errorlog,"PNG IHDR information:\n");
/*TODO*///				fprintf(errorlog,"Width: %i, Height: %i\n", p->width, p->height);
/*TODO*///				fprintf(errorlog,"Bit depth %i, color type: %i\n", p->bit_depth, p->color_type);
/*TODO*///				fprintf(errorlog,"Compression method: %i, filter: %i, interlace: %i\n",
/*TODO*///					p->compression_method, p->filter_method, p->interlace_method);
/*TODO*///			}
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_PLTE:
/*TODO*///			p->num_palette=chunk_length/3;
/*TODO*///			p->palette=chunk_data;
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"%i palette entries\n", p->num_palette);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_tRNS:
/*TODO*///			p->num_trans=chunk_length;
/*TODO*///			p->trans=chunk_data;
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"%i transparent palette entries\n", p->num_trans);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_IDAT:
/*TODO*///			idat[num_idat]=chunk_data;
/*TODO*///			l_idat[num_idat]=chunk_length;
/*TODO*///			num_idat++;
/*TODO*///			tot_idat += chunk_length;
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_tEXt:
/*TODO*///			if (errorlog)
/*TODO*///			{
/*TODO*///				UINT8 *text=chunk_data;
/*TODO*///
/*TODO*///				while(*text++);
/*TODO*///				chunk_data[chunk_length]=0;
/*TODO*///				fprintf(errorlog, "Keyword: %s\n", chunk_data);
/*TODO*///				fprintf(errorlog, "Text: %s\n", text);
/*TODO*///			}
/*TODO*///			free(chunk_data);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_tIME:
/*TODO*///			if (errorlog)
/*TODO*///			{
/*TODO*///				UINT8 *t=chunk_data;
/*TODO*///				fprintf(errorlog, "Image last-modification time: %i/%i/%i (%i:%i:%i) GMT\n",
/*TODO*///					((short)(*t) << 8)+ (short)(*(t+1)), *(t+2), *(t+3), *(t+4), *(t+5), *(t+6));
/*TODO*///			}
/*TODO*///
/*TODO*///			free(chunk_data);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_gAMA:
/*TODO*///			p->source_gamma	 = convert_from_network_order(chunk_data)/100000.0;
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog, "Source gamma: %f\n",p->source_gamma);
/*TODO*///
/*TODO*///			free(chunk_data);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_pHYs:
/*TODO*///			p->xres = convert_from_network_order(chunk_data);
/*TODO*///			p->yres = convert_from_network_order(chunk_data+4);
/*TODO*///			p->resolution_unit = *(chunk_data+8);
/*TODO*///			if (errorlog)
/*TODO*///			{
/*TODO*///				fprintf(errorlog, "Pixel per unit, X axis: %i\n",p->xres);
/*TODO*///				fprintf(errorlog, "Pixel per unit, Y axis: %i\n",p->yres);
/*TODO*///				if (p->resolution_unit)
/*TODO*///					fprintf(errorlog, "Unit is meter\n");
/*TODO*///				else
/*TODO*///					fprintf(errorlog, "Unit is unknown\n");
/*TODO*///			}
/*TODO*///			free(chunk_data);
/*TODO*///			break;
/*TODO*///
/*TODO*///		case PNG_CN_IEND:
/*TODO*///			break;
/*TODO*///
/*TODO*///		default:
/*TODO*///			if (errorlog)
/*TODO*///			{
/*TODO*///				if (chunk_type & 0x20000000)
/*TODO*///					fprintf (errorlog, "Ignoring ancillary chunk %s\n",str_chunk_type);
/*TODO*///				else
/*TODO*///					fprintf (errorlog, "Ignoring critical chunk %s!\n",str_chunk_type);
/*TODO*///			}
/*TODO*///			if (chunk_data)
/*TODO*///				free(chunk_data);
/*TODO*///			break;
/*TODO*///		}
/*TODO*///	}
/*TODO*///	if ((zimage = (UINT8 *)malloc(tot_idat))==NULL)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Out of memory\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	p->zlength = tot_idat;
/*TODO*///
/*TODO*///	/* combine idat chunks to compressed image data */
/*TODO*///	temp = zimage;
/*TODO*///	for (i=0; i<num_idat; i++)
/*TODO*///	{
/*TODO*///		memcpy (temp, idat[i], l_idat[i]);
/*TODO*///		free (idat[i]);
/*TODO*///		temp += l_idat[i];
/*TODO*///	}
/*TODO*///	p->zimage=zimage;
/*TODO*///	p->bpp = (samples[p->color_type] * p->bit_depth) / 8;
/*TODO*///	p->rowbytes = ceil((p->width * p->bit_depth * samples[p->color_type]) / 8.0);
/*TODO*///
/*TODO*///	if (png_inflate_image(p)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	if(png_unfilter (p)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*////*	Expands a p->image from p->bit_depth to 8 bit */
/*TODO*///int png_expand_buffer_8bit (struct png_info *p)
/*TODO*///{
/*TODO*///	int i,j, k;
/*TODO*///	UINT8 *inp, *outp, *outbuf;
/*TODO*///
/*TODO*///	if (p->bit_depth < 8)
/*TODO*///	{
/*TODO*///		if ((outbuf = (UINT8 *)malloc(p->width*p->height))==NULL)
/*TODO*///		{
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"Out of memory\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		inp = p->image;
/*TODO*///		outp = outbuf;
/*TODO*///
/*TODO*///		for (i = 0; i < p->height; i++)
/*TODO*///		{
/*TODO*///			for(j = 0; j < p->width / ( 8 / p->bit_depth); j++)
/*TODO*///			{
/*TODO*///				for (k = 8 / p->bit_depth-1; k >= 0; k--)
/*TODO*///					*outp++ = (*inp >> k * p->bit_depth) & (0xff >> (8 - p->bit_depth));
/*TODO*///				inp++;
/*TODO*///			}
/*TODO*///			if (p->width % (8 / p->bit_depth))
/*TODO*///			{
/*TODO*///				for (k = p->width % (8 / p->bit_depth)-1; k >= 0; k--)
/*TODO*///					*outp++ = (*inp >> k * p->bit_depth) & (0xff >> (8 - p->bit_depth));
/*TODO*///				inp++;
/*TODO*///			}
/*TODO*///		}
/*TODO*///		free (p->image);
/*TODO*///		p->image = outbuf;
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///void png_delete_unused_colors (struct png_info *p)
/*TODO*///{
/*TODO*///	int i, tab[256], pen=0, trns=0;
/*TODO*///	UINT8 ptemp[3*256], ttemp[256];
/*TODO*///
/*TODO*///	memset (tab, 0, 256*sizeof(int));
/*TODO*///	memcpy (ptemp, p->palette, 3*p->num_palette);
/*TODO*///	memcpy (ttemp, p->trans, p->num_trans);
/*TODO*///
/*TODO*///	/* check which colors are actually used */
/*TODO*///	for (i = 0; i < p->height*p->width; i++)
/*TODO*///		tab[p->image[i]]++;
/*TODO*///
/*TODO*///	/* shrink palette and transparency */
/*TODO*///	for (i = 0; i < p->num_palette; i++)
/*TODO*///		if (tab[i])
/*TODO*///		{
/*TODO*///			p->palette[3*pen+0]=ptemp[3*i+0];
/*TODO*///			p->palette[3*pen+1]=ptemp[3*i+1];
/*TODO*///			p->palette[3*pen+2]=ptemp[3*i+2];
/*TODO*///			if (i < p->num_trans)
/*TODO*///			{
/*TODO*///				p->trans[pen] = ttemp[i];
/*TODO*///				trns++;
/*TODO*///			}
/*TODO*///			tab[i] = pen++;
/*TODO*///		}
/*TODO*///
/*TODO*///	/* remap colors */
/*TODO*///	for (i = 0; i < p->height*p->width; i++)
/*TODO*///		p->image[i]=tab[p->image[i]];
/*TODO*///
/*TODO*///	if (errorlog && (p->num_palette!=pen))
/*TODO*///		fprintf (errorlog, "%i unused pen(s) deleted\n", p->num_palette-pen);
/*TODO*///
/*TODO*///	p->num_palette = pen;
/*TODO*///	p->num_trans = trns;
/*TODO*///}
/*TODO*///
/*TODO*////********************************************************************************
/*TODO*///
/*TODO*///  PNG write functions
/*TODO*///
/*TODO*///********************************************************************************/
/*TODO*///
/*TODO*///static void convert_to_network_order (UINT32 i, UINT8 *v)
/*TODO*///{
/*TODO*///	v[0]=i>>24;
/*TODO*///	v[1]=(i>>16)&0xff;
/*TODO*///	v[2]=(i>>8)&0xff;
/*TODO*///	v[3]=i&0xff;
/*TODO*///}
/*TODO*///
/*TODO*///static int png_write_chunk(void *fp, UINT32 chunk_type, UINT8 *chunk_data, UINT32 chunk_length)
/*TODO*///{
/*TODO*///	UINT32 crc;
/*TODO*///	UINT8 v[4];
/*TODO*///	int written;
/*TODO*///
/*TODO*///	/* write length */
/*TODO*///	convert_to_network_order(chunk_length, v);
/*TODO*///	written = osd_fwrite(fp, v, 4);
/*TODO*///
/*TODO*///	/* write type */
/*TODO*///	convert_to_network_order(chunk_type, v);
/*TODO*///	written += osd_fwrite(fp, v, 4);
/*TODO*///
/*TODO*///	/* calculate crc */
/*TODO*///	crc=crc32(0, v, 4);
/*TODO*///	if (chunk_length > 0)
/*TODO*///	{
/*TODO*///		/* write data */
/*TODO*///		written += osd_fwrite(fp, chunk_data, chunk_length);
/*TODO*///		crc=crc32(crc, chunk_data, chunk_length);
/*TODO*///	}
/*TODO*///	convert_to_network_order(crc, v);
/*TODO*///
/*TODO*///	/* write crc */
/*TODO*///	written += osd_fwrite(fp, v, 4);
/*TODO*///
/*TODO*///	if (written != 3*4+chunk_length)
/*TODO*///	{
/*TODO*///		if (errorlog) fprintf (errorlog, "Chunk write failed\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///static int png_write_file(void *fp, struct png_info *p)
/*TODO*///{
/*TODO*///	UINT8 ihdr[13];
/*TODO*///	char text[256];
/*TODO*///
/*TODO*///	/* PNG Signature */
/*TODO*///	if (osd_fwrite(fp, PNG_Signature, 8) != 8)
/*TODO*///	{
/*TODO*///		if (errorlog) fprintf (errorlog, "PNG sig write failed\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	/* PNG_CN_IHDR */
/*TODO*///	convert_to_network_order(p->width, ihdr);
/*TODO*///	convert_to_network_order(p->height, ihdr+4);
/*TODO*///	*(ihdr+8) = p->bit_depth;
/*TODO*///	*(ihdr+9) = p->color_type;
/*TODO*///	*(ihdr+10) = p->compression_method;
/*TODO*///	*(ihdr+11) = p->filter_method;
/*TODO*///	*(ihdr+12) = p->interlace_method;
/*TODO*///	if (errorlog) fprintf (errorlog, "Type(%d) Color Depth(%d)\n", p->color_type,p->bit_depth);
/*TODO*///	if (png_write_chunk(fp, PNG_CN_IHDR, ihdr, 13)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	/* PNG_CN_PLTE */
/*TODO*///	if (p->num_palette > 0)
/*TODO*///		if (png_write_chunk(fp, PNG_CN_PLTE, p->palette, p->num_palette*3)==0)
/*TODO*///			return 0;
/*TODO*///
/*TODO*///	/* PNG_CN_IDAT */
/*TODO*///	if (png_write_chunk(fp, PNG_CN_IDAT, p->zimage, p->zlength)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	/* PNG_CN_tEXt */
/*TODO*///	sprintf (text, "Software");
/*TODO*///#ifdef MESS
/*TODO*///	sprintf (text+9, "MESS %s", build_version);
/*TODO*///#else
/*TODO*///	sprintf (text+9, "MAME %s", build_version);
/*TODO*///#endif
/*TODO*///	if (png_write_chunk(fp, PNG_CN_tEXt, (UINT8 *)text, 14+strlen(build_version))==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	/* PNG_CN_IEND */
/*TODO*///	if (png_write_chunk(fp, PNG_CN_IEND, NULL, 0)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///static int png_filter(struct png_info *p)
/*TODO*///{
/*TODO*///	int i;
/*TODO*///	UINT8 *src, *dst;
/*TODO*///
/*TODO*///	if((p->fimage = (UINT8 *)malloc (p->height*(p->rowbytes+1)))==NULL)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Out of memory\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	dst = p->fimage;
/*TODO*///	src = p->image;
/*TODO*///
/*TODO*///	for (i=0; i<p->height; i++)
/*TODO*///	{
/*TODO*///		*dst++ = 0; /* No filter */
/*TODO*///		memcpy (dst, src, p->rowbytes);
/*TODO*///		src += p->rowbytes;
/*TODO*///		dst += p->rowbytes;
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///static int png_deflate_image(struct png_info *p)
/*TODO*///{
/*TODO*///	unsigned long zbuff_size;
/*TODO*///
/*TODO*///	zbuff_size = (p->height*(p->rowbytes+1))*1.1+12;
/*TODO*///
/*TODO*///	if((p->zimage = (UINT8 *)malloc (zbuff_size))==NULL)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Out of memory\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///
/*TODO*///	if (compress(p->zimage, &zbuff_size, p->fimage, p->height*(p->rowbytes+1)) != Z_OK)
/*TODO*///	{
/*TODO*///		if (errorlog)
/*TODO*///			fprintf(errorlog,"Error while deflating image\n");
/*TODO*///		return 0;
/*TODO*///	}
/*TODO*///	p->zlength = zbuff_size;
/*TODO*///
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///static int png_pack_buffer (struct png_info *p)
/*TODO*///{
/*TODO*///	UINT8 *outp, *inp;
/*TODO*///	int i,j,k;
/*TODO*///
/*TODO*///	outp = inp = p->image;
/*TODO*///
/*TODO*///	if (p->bit_depth < 8)
/*TODO*///	{
/*TODO*///		for (i=0; i<p->height; i++)
/*TODO*///		{
/*TODO*///			for(j=0; j<p->width/(8/p->bit_depth); j++)
/*TODO*///			{
/*TODO*///				for (k=8/p->bit_depth-1; k>=0; k--)
/*TODO*///					*outp |= *inp++ << k * p->bit_depth;
/*TODO*///				outp++;
/*TODO*///				*outp = 0;
/*TODO*///			}
/*TODO*///			if (p->width % (8/p->bit_depth))
/*TODO*///			{
/*TODO*///				for (k=p->width%(8/p->bit_depth)-1; k>=0; k--)
/*TODO*///					*outp |= *inp++ << k * p->bit_depth;
/*TODO*///				outp++;
/*TODO*///				*outp = 0;
/*TODO*///			}
/*TODO*///		}
/*TODO*///	}
/*TODO*///	return 1;
/*TODO*///}
/*TODO*///
/*TODO*///
/*TODO*////*********************************************************************
/*TODO*///
/*TODO*///  Writes an osd_bitmap in a PNG file. If the depth of the bitmap
/*TODO*///  is 8, a color type 3 PNG with palette is written. Otherwise a
/*TODO*///  color type 2 true color RGB PNG is written.
/*TODO*///
/*TODO*/// *********************************************************************/
    public static int png_write_bitmap(Object fp, osd_bitmap bitmap) {
        /*TODO*///	int i, j, c;
/*TODO*///	UINT8 *ip;
/*TODO*///	struct png_info p;
/*TODO*///
/*TODO*///	memset (&p, 0, sizeof (struct png_info));
/*TODO*///	p.xscale = p.yscale = p.source_gamma = 0.0;
/*TODO*///	p.palette = p.trans = p.image = p.zimage = p.fimage = NULL;
/*TODO*///	p.width = bitmap->width;
/*TODO*///	p.height = bitmap->height;
/*TODO*///	p.color_type = (bitmap->depth == 8 ? 3: 2);
/*TODO*///
/*TODO*///	if (p.color_type == 3)
/*TODO*///	{
/*TODO*///		if((p.palette = (UINT8 *)malloc (3*256))==NULL)
/*TODO*///		{
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"Out of memory\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///		memset (p.palette, 0, 3*256);
/*TODO*///		/* get palette */
/*TODO*///		for (i = 0; i < Machine->drv->total_colors; i++)
/*TODO*///		{
/*TODO*///			c = Machine->pens[i];
/*TODO*///			osd_get_pen(c,&p.palette[3*c],&p.palette[3*c+1],&p.palette[3*c+2]);
/*TODO*///		}
/*TODO*///
/*TODO*///		p.num_palette = 256;
/*TODO*///		if((p.image = (UINT8 *)malloc (p.height*p.width))==NULL)
/*TODO*///		{
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"Out of memory\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		for (i = 0; i < p.height; i++)
/*TODO*///			memcpy(&p.image[i * p.width], bitmap->line[i], p.width);
/*TODO*///
/*TODO*///		png_delete_unused_colors (&p);
/*TODO*///		p.bit_depth = p.num_palette > 16 ? 8 : p.num_palette > 4 ? 4 : p.num_palette > 2 ? 2 : 1;
/*TODO*///		p.rowbytes=ceil((p.width*p.bit_depth)/8.0);
/*TODO*///		if (png_pack_buffer (&p) == 0)
/*TODO*///			return 0;
/*TODO*///
/*TODO*///	}
/*TODO*///	else
/*TODO*///	{
/*TODO*///		p.rowbytes = p.width * 3;
/*TODO*///		p.bit_depth = 8;
/*TODO*///		if((p.image = (UINT8 *)malloc (p.height * p.rowbytes))==NULL)
/*TODO*///		{
/*TODO*///			if (errorlog)
/*TODO*///				fprintf(errorlog,"Out of memory\n");
/*TODO*///			return 0;
/*TODO*///		}
/*TODO*///
/*TODO*///		ip = p.image;
/*TODO*///
/*TODO*///		for (i = 0; i < p.height; i++)
/*TODO*///			for (j = 0; j < p.width; j++)
/*TODO*///			{
/*TODO*///				osd_get_pen(((UINT16 *)bitmap->line[i])[j],ip, ip+1, ip+2);
/*TODO*///				ip += 3;
/*TODO*///			}
/*TODO*///	}
/*TODO*///
/*TODO*///	if(png_filter (&p)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	if (png_deflate_image(&p)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	if (png_write_file(fp, &p)==0)
/*TODO*///		return 0;
/*TODO*///
/*TODO*///	if (p.palette) free (p.palette);
/*TODO*///	if (p.image) free (p.image);
/*TODO*///	if (p.zimage) free (p.zimage);
/*TODO*///	if (p.fimage) free (p.fimage);
        return 1;
    }
}
