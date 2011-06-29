package at.tuwien.dsg.util;

import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.content.res.XmlResourceParser;
import at.tuwien.dsg.common.TweetflowFilter;

public class TweetFilterParser {
	public List<TweetflowFilter> parse(XmlResourceParser xmlResourceParser) {
        List<TweetflowFilter> primitives = null;
        
        try {
            int eventType = xmlResourceParser.getEventType();
            TweetflowFilter currentPrimitive = null;
            boolean done = false;
            while (eventType != XmlPullParser.END_DOCUMENT && !done){
                String name = null;
                switch (eventType){
                    case XmlPullParser.START_DOCUMENT:
                    	primitives = new ArrayList<TweetflowFilter>();
                        break;
                    case XmlPullParser.START_TAG:
                        name = xmlResourceParser.getName();
                        if (name.equalsIgnoreCase("primitive")){
                        	currentPrimitive = new TweetflowFilter();
                        } else if (currentPrimitive != null){
                            /*if (name.equalsIgnoreCase("qualifier")){
                            	currentPrimitive.setQualifier(xmlResourceParser.nextText());
                            } else if (name.equalsIgnoreCase("name")){
	                        	currentPrimitive.setName(xmlResourceParser.nextText());
			                }else if (name.equalsIgnoreCase("description")){
                            	currentPrimitive.setDescription(xmlResourceParser.nextText());
	                        } else if (name.equalsIgnoreCase("pattern")){
	                        	currentPrimitive.setPattern(xmlResourceParser.nextText());
			                }
                            else {
                            	
                            }*/
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = xmlResourceParser.getName();
                        if (name.equalsIgnoreCase("primitive") && currentPrimitive != null){
                        	primitives.add(currentPrimitive);
                        } else if (name.equalsIgnoreCase("primitives")){
                            done = true;
                        }
                        break;
                }
                eventType = xmlResourceParser.next();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return primitives;
    }

}
