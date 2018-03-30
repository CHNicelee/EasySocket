package client;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by ice on 2018/3/29.
 */
public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        ArrayList<Integer> integers = new ArrayList<>();
        String line = scanner.nextLine();
        line = line.replace("　","");
        String[] ints = line.replaceAll("\\s{1,}", " ").split(" ");

        for (int i = 0; i < ints.length; i++) {
            if(ints[i].trim().length()>0)
            integers.add(Integer.valueOf(ints[i].trim()));
        }
        int n = integers.size()*(integers.size()-1);
        while (n-->0){

            integers.add(scanner.nextInt());
        }
        n = (int) Math.sqrt(integers.size());

        for(int i =n-1;i>=0;i--){
            int j = integers.size()-i-1;
            while(j>=0){
                System.out.print(integers.get(j));
                if(j>=n || i!=0)System.out.print(" ");
                j-=n;
            }
            if(i>0)
                System.out.println();
        }

    }
}