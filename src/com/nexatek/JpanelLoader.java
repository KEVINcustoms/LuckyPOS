/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nexatek;

import java.awt.BorderLayout;
import javax.swing.JPanel;

/**
 *
 * @author coolsasisndu
 */
public class JpanelLoader {
   
   public  void jPanelLoader(JPanel Main,JPanel setPanel){
      Main.removeAll();
      Main.setLayout(new BorderLayout());
      Main.add(setPanel, BorderLayout.CENTER);
      Main.revalidate();
      Main.repaint();
        System.gc();
    
    }
   
}
