/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulation;

/**
 *
 * @author osman
 */
public class Util {
    public static int[] discretizeSol(double[] sol) {
        int[] discreteSol = new int[sol.length];

        for (int i = 0; i < sol.length; i++) {
            discreteSol[i] = (int) sol[i];
        }

        return discreteSol;
    }
}
