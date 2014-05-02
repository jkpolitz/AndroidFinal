package com.example.upresent;

import android.graphics.Bitmap;

public class Presentation {
	public String name;
	public String author;
	public String date;
	public int presId;
	public Bitmap [] imgs;
	public String [] urls;
	
	public Presentation () {
		super();
	}
	
	public Presentation(String name, int presID){
		this.name = name;
		this.presId = presID;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
