/*****************************************************************************
 * Copyright 2007-2015 DCA-FEEC-UNICAMP
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *    Klaus Raizer, Andre Paraense, Ricardo Ribeiro Gudwin
 *****************************************************************************/

package codelets.motor;

import org.json.JSONException;
import org.json.JSONObject;

import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import java.util.Random;
import java.util.logging.Logger;
import ws3dproxy.model.Creature;

/**
 *  Hands Action Codelet monitors working storage for instructions and acts on the World accordingly.
 *  
 * @author klaus
 *
 */
public class BodyActionCodelet extends Codelet
{
    private MemoryObject bodyMO;
    private double previousTargetx = 0;
    private double previousTargety = 0;
    private String previousBodyAction="";
    private Creature c;
    private Random r = new Random();
    static Logger log = Logger.getLogger(BodyActionCodelet.class.getCanonicalName());

    public BodyActionCodelet(Creature nc) 
    {
        c = nc;
        this.setTimeStep(50);
    }
	
    @Override
    public void accessMemoryObjects() 
    {
       bodyMO = (MemoryObject)this.getInput("BODY");
    }
        
    public void proc() 
    {    
        String command = (String) bodyMO.getI();

        if (!command.equals("") && (!command.equals(previousBodyAction)))
        {
	    JSONObject jsonAction;
            try 
            {
                jsonAction = new JSONObject(command);
                String action = jsonAction.getString("ACTION");
                System.out.println("BodyAction.proc: received "+action+" from "+command);
		if (jsonAction.has("ACTION") && jsonAction.has("OBJECT"))
                {
		    String objectName = jsonAction.getString("OBJECT");
		    if (action.equals("PICKUP") || action.equals("SACKIT"))
                    {
                        try 
                        {
                            c.putInSack(objectName);
                            System.out.println("BodyAction.proc sackit "+objectName);
                        } catch (Exception e) 
                          {
                            System.out.println("BodyAction.proc sackit: "+e.getMessage());                
                          } 
	                log.info("Sending Put In Sack command to agent:****** "+objectName+"**********");																			
			//							}
		    }
		    if (action.equals("EATIT"))
                    {
                        try 
                        {
                            c.eatIt(objectName);
                        } catch (Exception e) 
                           {
                                                   
                           }
 		        log.info("Sending Eat command to agent:****** "+objectName+"**********");							 
		    }
		    if (action.equals("BURY") || action.equals("HIDEIT"))
                    {
                        try 
                        {
                            c.hideIt(objectName);
                        } catch (Exception e) 
                          {                                                    
                          }
   		        log.info("Sending Bury command to agent:****** "+objectName+"**********");							
		    }
            }  // end if Action
                    if (action.equals("FORAGE"))
                    {
                        if (!command.equals(previousBodyAction)) 
                            log.info("Sending Forage command to agent");
                        try {  
                            c.rotate(2);     
                            } catch (Exception e) 
                              {
                                e.printStackTrace();
                              }
			    }
                        else if (action.equals("GOTO"))
                        {
                            if (!command.equals(previousBodyAction)) 
                            {
                                double speed = jsonAction.getDouble("SPEED");
			        double targetx = jsonAction.getDouble("X");
			        double targety = jsonAction.getDouble("Y");
 			        if (!command.equals(previousBodyAction))
                                       log.info("Sending move command to agent: ["+targetx+","+targety+"]");
                                try {
                                     c.moveto(speed, targetx, targety);
                                    } catch(Exception e) {
                                               e.printStackTrace();
                                            }
				previousTargetx = targetx;
				previousTargety = targety;
                            }                   
		}
	} catch (JSONException e) {
		e.printStackTrace();
	}

    }
//  System.out.println("OK_hands");
    previousBodyAction = (String) bodyMO.getI();
}//end proc

  @Override
  public void calculateActivation() 
  {
        
  }


}
