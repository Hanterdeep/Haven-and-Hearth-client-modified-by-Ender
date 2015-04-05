package addons;

import java.util.ArrayList;

import haven.Coord;
import haven.Inventory;
import haven.Item;
import haven.Config;


public class RunFlaskScript extends Thread{
	HavenUtil m_util;
	static boolean m_filling = false;
	static public float m_fillFlaskAmount = 0.1f;
	
	public RunFlaskScript(HavenUtil util){
		m_util = util;
	}
	
	void staminaLoop(){
		Item flask = null;
		int flaskID = -1;
		int cancelID = -1;
		int count = 3;
		int bar = 1;
		int slot = 1;
		
		Coord flaskCoord = m_util.flaskToCoord(Config.flaskNum);
		m_fillFlaskAmount = (float)(Config.flaskFill / 10f);
		if(flaskCoord != null){
			bar = flaskCoord.x;
			slot = flaskCoord.y;
		}
		
		
		while(m_util.pathDrinker){
			m_util.wait(300);
			
			if(flask == null){
				if(Config.debug) System.out.println("debug 5");
				flask = m_util.findFlask();
				
				if(flask == null) continue;
				
				if(flaskID == -1) flaskID = flask.id;
				if(cancelID == flask.id ) flask = null;
				
				continue;
			}else if(flask != null && cancelID != flask.id){
				if(Config.debug) System.out.println("debug 6");
				
				if(!m_util.findFlaskToolbar(bar, slot) ){
					if(!findFlaskInBag(flaskID)){
						flask = null;
						flaskID = -1;
						continue;
					}
					
					m_util.setBeltSlot(bar, slot, flask);
					
					cancelID = flask.id;
					flaskID = flask.id;
					flask = null;
				}
			}
			
			if(m_util.findFlaskToolbar(bar, slot) && m_util.runFlask){
				if(Config.debug) System.out.println("debug 2");
				if(fillFlasks()) count = 3;
				
				if( (!Config.flaskFillOnly || m_util.running) && m_util.checkPlayerWalking() && !m_util.hasHourglass() && m_util.getStamina() < 80 && count > 2){
					Config.forcemod = false;
					m_util.useActionBar(bar, slot);
					count = 0;
					if(Config.debug) System.out.println("debug 7");
				}else{
					count++;
				}
			}
			
			if(Config.debug) System.out.println("debug 1");
		}
	}
	
	boolean fillFlasks(){
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		ArrayList<Item> flaskList = new ArrayList<Item>();
		if(Config.debug) System.out.println("debug 3");
		
		if(itemList == null) return false;
		
		for(Item i : itemList){
			String name = i.GetResName();
			if(name.contains("waterskin") || name.contains("waterflask") ){
				//System.out.println(i.olcol);
				if(i.olcol == null) continue;
				
				if(m_util.waterFlaskInfo(i) <= m_fillFlaskAmount){
					flaskList.add(i);
				}
			}
		}
		
		if(flaskList.size() > 0 && !m_filling && !m_util.flowerMenuReady()){
			m_filling = true;
			fillFlaskList(flaskList);
			m_filling = false;
			return true;
		}
		return false;
	}
	
	void fillFlaskList(ArrayList<Item> flaskList){
		boolean holding = false;
		if(Config.debug) System.out.println("debug 4");
		
		Inventory bag = m_util.getInventory("Inventory");
		
		Item waterBucket = m_util.getItemFromBag("bucket-water");
		
		if(waterBucket == null){
			return;
		}
		
		Coord bucketC = new Coord(waterBucket.c);
		
		if(m_util.mouseHoldingAnItem() ) holding = true;
		
		if(holding){
			bag.drop(new Coord(0,0), bucketC);
		}else if(m_util.getInventory("Inventory") != null && waterBucket != null){
			if(waterBucket != null) m_util.pickUpItem(waterBucket);
		}
		
		for(Item flask : flaskList)
			m_util.itemInteract(flask);
		
		bag.drop(new Coord(0,0), bucketC);
	}
	
	boolean findFlaskInBag(int id){
		ArrayList<Item> itemList = m_util.getItemsFromBag();
		
		for(Item i : itemList){
			if(i.id == id){
				//String name = i.GetResName();
				//if(name.contains("waterskin") || name.contains("waterflask") ){
					return true;
				//}
			}
		}
		
		return false;
	}
	
	public void run(){
		staminaLoop();
		m_util.runFlaskRunning = false;
	}
}