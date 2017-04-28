/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.com.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Cleison Douglas
 */
public class TabelaSimbolos {

    public HashMap TabelaSimbolos = new HashMap<>();

    public HashMap<String, String> getTabela() throws FileNotFoundException, IOException {
        File filesimbolos = new File("./tabelasimbolos.txt");
        BufferedReader tabsimbolos = new BufferedReader(new FileReader(filesimbolos));

        String linha;

        while (tabsimbolos.ready()) {
            linha = tabsimbolos.readLine();
            if (linha.equals("")) {
                continue;
            }
            TabelaSimbolos.put(linha.substring(0, linha.indexOf(',')), linha.substring(linha.indexOf(' ') + 1, linha.length()));
        }
        return TabelaSimbolos;
    }

    public void setTabela(String tipo, String lexema) throws IOException {
        File filesimbolos = new File("./tabelasimbolos.txt");
        FileWriter fileWriter = new FileWriter(filesimbolos, true);
        PrintWriter simbolos = new PrintWriter(fileWriter);
        simbolos.println(tipo + ", " + lexema);
        simbolos.close();
        TabelaSimbolos = getTabela();
    }
    
    public void resetTabela(){
        /* 
        método para resetar a tabela de simbolos.
        ideia principal: o arquivo tabelasimbolos.txt terá seu conteúdo substituido pelo de
        tabelasimbolosBKP.txt, porque a cada execução o Anaisador Sintatico adiciona
        os ID's criados na tabela, porém, a cada NOVA execução, o arquivo deve ser
        iniciado em seu formato base, desse modo não havendo conflito de ID's.
        */
    }
}
