/*
 * Copyright (C) 2017 ftanada.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package codelets.perception;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import memory.CreatureInnerSense;
import ws3dproxy.model.Thing;

/**
 *
 * @author ftanada
 */
public class ClosestJewelDetector extends Codelet 
{
	private MemoryObject knownMO;
	private MemoryObject closestJewelMO;
	private MemoryObject innerSenseMO;
	
        private List<Thing> known;

	public ClosestJewelDetector() 
        {
	}


	@Override
	public void accessMemoryObjects() 
        {
		this.knownMO=(MemoryObject)this.getInput("KNOWN_JEWELS");
		this.innerSenseMO=(MemoryObject)this.getInput("INNER");
		this.closestJewelMO=(MemoryObject)this.getOutput("CLOSEST_JEWEL");	
	}
        
	@Override
	public void proc() 
        {
            Thing closest_jewel = null;
            known = Collections.synchronizedList((List<Thing>) knownMO.getI());
            CreatureInnerSense cis = (CreatureInnerSense) innerSenseMO.getI();
            synchronized(known) 
            {
	        if (known.size() != 0)
                {
		    //Iterate over objects in vision, looking for the closest apple
                    CopyOnWriteArrayList<Thing> myknown = new CopyOnWriteArrayList<>(known);
                    for (Thing t : myknown) 
                    {
				String objectName=t.getName();
				if (objectName.contains("JEWEL") )
                                { //Then, it is an apple
                                        if (closest_jewel == null)
                                        {    
                                                closest_jewel = t;
					}
                                        else {
						double Dnew = calculateDistance(t.getX1(), t.getY1(), cis.position.getX(), cis.position.getY());
                                                double Dclosest= calculateDistance(closest_jewel.getX1(), closest_jewel.getY1(), cis.position.getX(), cis.position.getY());
						if (Dnew<Dclosest)
                                                {
                                                        closest_jewel = t;
						}
					}
				}
		    }  // end for
                        
                    if (closest_jewel != null)
                    {    
			if (closestJewelMO.getI() == null || 
                           !closestJewelMO.getI().equals(closest_jewel))
                            closestJewelMO.setI(closest_jewel);
		    }				
                    else
                    {
			//couldn't find any nearby apples
                        closest_jewel = null;
                        closestJewelMO.setI(closest_jewel);		   
		   }
                }
                else
                { // if there are no known apples closest_apple must be null
                        closest_jewel = null;
                        closestJewelMO.setI(closest_jewel);
		}
            }
	} //end proc

@Override
  public void calculateActivation() 
  {
        
  }
        
  private double calculateDistance(double x1, double y1, double x2, double y2) 
  {
    return(Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2)));
  }

}

