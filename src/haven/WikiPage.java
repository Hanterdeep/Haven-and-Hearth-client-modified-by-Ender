package haven;

import haven.RichText.Foundry;

import java.awt.Color;
import java.awt.font.TextAttribute;

import wikilib.Request;
import wikilib.RequestCallback;
import wikilib.WikiLib;

public class WikiPage extends HWindow {
    private static final Foundry fnd = new Foundry(TextAttribute.FOREGROUND, Color.BLACK);
    private RichTextBox content;
    private WikiLib reader;
    private RequestCallback callback;
    
    public WikiPage(Widget parent, String request, boolean closable) {
	super(parent, request, closable);
	content = new RichTextBox(Coord.z, sz, this, "", fnd);
	content.bg = new Color(255, 255, 255, 128);
	content.registerclicks = true;
	
	final HWindow wnd = this;
	
	callback = new RequestCallback() {
	    public void run(Request req) {
		synchronized (content) {
		    content.settext(req.result);
		    if(req.title != null) {
			title = req.title;
			ui.wiki.updurgency(wnd, 0);
		    }
		}
	    }
	};
	Request req = new Request(request, callback);
	if(request.indexOf("/wiki/")>=0) {
	    request = request.replaceAll("/wiki/", "");
	    req.initPage(request);
	}
	reader = new WikiLib();
	reader.search(req);
	if(cbtn != null) {
	    cbtn.raise();
	}
    }
    
    public void setsz(Coord s) {
	super.setsz(s);
	content.setsz(sz);
    }
    
    public void draw(GOut g) {
	super.draw(g);
    }
    
    public void wdgmsg(Widget sender, String msg, Object... args) {
	if(sender == content) {
	    new WikiPage(ui.wiki, (String)args[0], true);
	} else if(sender == cbtn) {
	    ui.destroy(this);
	} else {
	    super.wdgmsg(sender, msg, args);
	}
    }
    
    public void destroy() {
	super.destroy();
	callback = null;
	reader = null;;
    }
}