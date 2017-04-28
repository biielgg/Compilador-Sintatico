/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.com.controller;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Heriklys
 */
public class AnalisadorSintatico {

    public static List<Token> listaTokens = new LinkedList<>();
    public static boolean result = true;
    public static HashMap<String, String> tabelaSimbolos = new HashMap<>();
    static TabelaSimbolos ts = new TabelaSimbolos();
    static int contError;
    static String e1 = "token";
    static String e2 = "duplicidade";
    static String e3 = "declaração";

    public static void main(String[] args) throws IOException {
        tabelaSimbolos.putAll(ts.getTabela());
        while (!listaTokens.isEmpty() && contError < 5) {
            Programa();
        }
        System.out.println("Arquivo contém: " + contError + " erros.");
    }

    static void Programa() {
        Classe();
        //EOF;
    }

    static void Classe() {
        if (casaToken("class")) {
            //espera-se um ID
            if (casaToken("ID")) {
                if (casaToken(":")) {
                    ListaFuncao();
                    Main();
                    if (casaToken("end")) {
                        if (!casaToken(".")) {
                            Error(e1, ".");
                        }
                    } else {
                        Error(e1, "end");
                    }
                } else {
                    Error(e1, ":");
                }
            } else {
                Error(e1, "ID");
            }
        } else {
            Error(e1, "class");
        }
    }

    static void ListaFuncao() {
        Funcao();
    }

    static void Funcao() {
        if (casaToken("def")) {
            TipoMacro();
            //espera-se um ID
            if (isToken("ID")) {
                if (!verificaTabela(listaTokens.get(0).getLexema())) {
                    try {
                        ts.setTabela(listaTokens.get(0).getTipo(), listaTokens.get(0).getLexema());
                    } catch (IOException ex) {
                        Logger.getLogger(AnalisadorSintatico.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    casaToken("ID");
                    if (casaToken("(")) {
                        ListaArg();
                        if (casaToken(")")) {
                            if (casaToken(":")) {
                                DeclaraVariasID();
                                ListaCmd();
                                Retorno();
                                if (casaToken("end")) {
                                    if (casaToken(";")) {
                                        Funcao();
                                    } else {
                                        Error(e1, ";");
                                    }
                                } else {
                                    Error(e1, "end");
                                }
                            } else {
                                Error(e1, ":");
                            }
                        } else {
                            Error(e1, ")");
                        }
                    } else {
                        Error(e1, "(");
                    }
                } else {
                    Error(e2, listaTokens.get(0).getLexema());
                }
            } else {
                Error(e1, "ID");
            }
        }
    }

    static void TipoMacro() {
        TipoPrimitivo();
        TP();
    }

    static void TipoPrimitivo() {
        if (!casaToken("bool") && !casaToken("integer") && !casaToken("String") && !casaToken("double") && !casaToken("void")) {
            Error(e1, "Tipo Primitivo");
        }
    }

    static void TP() {
        if (casaToken("[")) {
            if (!casaToken("]")) {
                Error(e1, "]");
            }
        }
    }

    static void ListaArg() {
        Arg();
        LA();
    }

    static void Arg() {
        TipoMacro();
        //espera-se um ID  -- DECLARAÇÃO DE ID EM PARAMETROS DE FUNÇÕES
        if (isToken("ID")) {
            try {
                ts.setTabela(listaTokens.get(0).getTipo(), listaTokens.get(0).getLexema());
            } catch (IOException ex) {
                Logger.getLogger(AnalisadorSintatico.class.getName()).log(Level.SEVERE, null, ex);
            }
                casaToken("ID");
            
        } else {
            Error(e1, "ID");
        }
    }

    static void LA() {
        if (casaToken(",")) {
            ListaArg();
        }
    }

    static void DeclaraVariasID() {
        DeclaraID();
    }

    static void DeclaraID() {
        if (isToken("bool") || isToken("integer") || isToken("String") || isToken("double") || isToken("void")) {
            TipoMacro();
            if (isToken("ID")) {
                if (!verificaTabela(listaTokens.get(0).getLexema())) {
                    try {
                        ts.setTabela(listaTokens.get(0).getTipo(), listaTokens.get(0).getLexema());
                    } catch (IOException ex) {
                        Logger.getLogger(AnalisadorSintatico.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    casaToken("ID");
                    if (!casaToken(";")) {
                        Error(e1, ";");
                    } else {
                        DeclaraID();
                    }
                } else {
                    Error(e2, listaTokens.get(0).getLexema());
                }
            }
        }
    }

    static void ListaCmd() {
        Cmd();
    }

    static void Cmd() {
        if (isToken("if")) {
            CmdIF();
            Cmd();
        } else if (isToken("while")) {
            CmdWhile();
            Cmd();
        } else if (isToken("ID")) {
            if (verificaTabela(listaTokens.get(0).getLexema())) {
                casaToken("ID");
                if (isToken("=")) {
                    CMA();
                    Cmd();
                } else if (isToken("(")) {
                    CmdFuncao();
                    Cmd();
                } else {
                    //Token inesperado, não veio '=' nem '('
                    Error(e1, "");
                }

            } else {
                Error(e3, listaTokens.get(0).getLexema());
            }
        } else if (isToken("write")) {
            CmdWrite();
            Cmd();
        } else if (isToken("writeln")) {
            CmdWriteln();
            Cmd();
        }
    }

    static void CmdIF() {
        if (casaToken("if")) {
            if (casaToken("(")) {
                Expressao();
                if (casaToken(")")) {
                    if (casaToken(":")) {
                        ListaCmd();
                        CIF();
                    } else {
                        Error(e1, ":");
                    }
                } else {
                    Error(e1, ")");
                }
            } else {
                Error(e1, "(");
            }
        } else {
            Error(e1, "if");
        }
    }

    static void CIF() {
        if (casaToken("end")) {
            if (!casaToken(";")) {
                Error(e1, ";");
            }
        } else if (casaToken("else")) {
            ListaCmd();
            if (casaToken("end")) {
                if (!casaToken(";")) {
                    Error(e1, ";");
                }
            } else {
                Error(e1, "end");
            }
        } else {
            //Token inesperado, não veio end nem else;
            Error(e1, "");
        }
    }

    static void CmdWhile() {
        if (casaToken("while")) {
            if (casaToken("(")) {
                Expressao();
                if (casaToken(")")) {
                    if (casaToken(":")) {
                        ListaCmd();
                        if (casaToken(";")) {
                        } else {
                            Error(e1, ";");
                        }
                    } else {
                        Error(e1, ":");
                    }
                } else {
                    Error(e1, ")");
                }
            } else {
                Error(e1, "(");
            }
        } else {
            Error(e1, "while");
        }
    }

    static void CMA() {
        if (casaToken("=")) {
            Expressao();
            if (!casaToken(";")) {
                Error(e1, ";");
            }
        } else if (casaToken("[")) {
            Expressao();
            if (casaToken("]")) {
                if (casaToken("=")) {
                    Expressao();
                    if (!casaToken(";")) {
                        Error(e1, ";");
                    }
                } else {
                    Error(e1, "=");
                }
            } else {
                Error(e1, "]");
            }
        } else {
            Error(e1, "");
        }
    }

    static void CmdFuncao() {
        //espera-se um ID
        if (isToken("ID")) {
            if (verificaTabela(listaTokens.get(0).getLexema())) {
                casaToken("ID");
                if (casaToken("(")) {
                    VariasExpressao();
                    if (casaToken(")")) {
                        if (!casaToken(";")) {
                            Error(e1, ";");
                        }
                    } else {
                        Error(e1, ")");
                    }
                } else {
                    Error(e1, "(");
                }
            } else {
                Error(e3, listaTokens.get(0).getLexema());
            }
        } else {
            Error(e1, "ID");
        }
    }

    static void CmdWrite() {
        if (casaToken("write")) {
            if (casaToken("(")) {
                Expressao();
                if (casaToken(")")) {
                    if (!casaToken(";")) {
                        Error(e1, ";");
                    }
                } else {
                    Error(e1, ")");
                }
            } else {
                Error(e1, "(");
            }
        } else {
            Error(e1, "write");

        }
    }

    static void CmdWriteln() {
        if (casaToken("writeln")) {
            if (casaToken("(")) {
                Expressao();
                if (casaToken(")")) {
                    if (!casaToken(";")) {
                        Error(e1, ";");
                    }
                } else {
                    Error(e1, ")");
                }
            } else {
                Error(e1, "(");
            }
        } else {
            Error(e1, "writeln");
        }
    }

    static void VariasExpressao() {
        if (isToken("or") || isToken("and") || isToken("==") || isToken("!=") || isToken("<") || isToken("<=")
                || isToken(">") || isToken(">=") || isToken("-") || isToken("+") || isToken("*") || isToken("/")
                || isToken("!") || isToken("(") || isToken(")") || isToken("ID") || isToken("ConstInteger")
                || isToken("ConstDouble") || isToken("ConstString") || isToken("true") || isToken("false")
                || isToken("vector")) {
            ExpressaoDentro();
        }
    }

    static void ExpressaoDentro() {
        Expressao();
        ED();
    }

    static void ED() {
        if (casaToken(",")) {
            ExpressaoDentro();
        }
    }

    static void Expressao() {
        Expressao1();
        Exp();
    }

    static void Exp() {
        if (casaToken("or")) {
            Expressao1();
            Exp();
        }
    }

    static void Expressao1() {
        Expressao2();
        Exp1();
    }

    static void Exp1() {
        if (casaToken("and")) {
            Expressao2();
            Exp1();
        }
    }

    static void Expressao2() {
        Expressao3();
        Exp2();
    }

    static void Exp2() {
        if (casaToken("==")) {
            Expressao3();
            Exp2();
        } else if (casaToken("!=")) {
            Expressao3();
            Exp2();
        }
    }

    static void Expressao3() {
        Expressao4();
        Exp3();
    }

    static void Exp3() {
        if (casaToken("<")) {
            Expressao4();
            Exp3();
        } else if (casaToken("<=")) {
            Expressao4();
            Exp3();
        } else if (casaToken(">")) {
            Expressao4();
            Exp3();
        } else if (casaToken(">=")) {
            Expressao4();
            Exp3();
        }
    }

    static void Expressao4() {
        Expressao5();
        Exp4();
    }

    static void Exp4() {
        if (casaToken("-")) {
            Expressao5();
            Exp4();
        } else if (casaToken("+")) {
            Expressao5();
            Exp4();
        }
    }

    static void Expressao5() {
        Expressao6();
        Exp5();
    }

    static void Exp5() {
        if (casaToken("/")) {
            Expressao6();
            Exp5();
        } else if (casaToken("*")) {
            Expressao6();
            Exp5();
        }
    }

    static void Expressao6() {
        if (isToken("!")) {
            OpUnario();
            Expressao7();
        }
        Expressao7();
    }

    static void Expressao7() {
        if (casaToken("(")) {
            Expressao();
            if (!casaToken(")")) {
                Error(e1, ")");
            }
        } else {
            Expressao8();
        }
    }

    static void Expressao8() {
        if (isToken("ID")) {
            if (verificaTabela(listaTokens.get(0).getLexema())) {
                casaToken("ID");
                E();
            } else {
                Error(e3, listaTokens.get(0).getLexema());
            }
        } else if (casaToken("ConstInteger")) {
        } else if (casaToken("ConstString")) {
        } else if (casaToken("ConstDouble")) {
        } else if (casaToken("true")) {
        } else if (casaToken("false")) {
        } else if (casaToken("vector")) {
            TipoPrimitivo();
            if (casaToken("[")) {
                Expressao();
                if (!casaToken("]")) {
                    Error(e1, "]");
                }
            } else {
                Error(e1, "[");
            }
        } else {
            Error(e1, "");
        }
    }

    static void OpUnario() {
        if (casaToken("-")) {
        } else if (!casaToken("!")) {
            Error(e1, "");
        }
    }

    static void E() {
        if (casaToken("[")) {
            Expressao();
            if (!casaToken("]")) {
                Error(e1, "]");
            }
        } else if (casaToken("(")) {
            VariasExpressao();
            if (!casaToken(")")) {
                Error(e1, ")");
            }
        }
    }

    static void Retorno() {
        if (casaToken("return")) {
            Expressao();
            if (!casaToken(";")) {
                Error(e1, ";");
            }
        }
    }

    static void Main() {
        if (casaToken("defstatic")) {
            if (casaToken("void")) {
                if (casaToken("main")) {
                    if (casaToken("(")) {
                        if (casaToken("String")) {
                            if (casaToken("[")) {
                                if (casaToken("]")) {
                                    //espera-se um ID
                                    if (isToken("ID")) {
                                        try {
                                            ts.setTabela(listaTokens.get(0).getTipo(), listaTokens.get(0).getLexema());
                                        } catch (IOException ex) {
                                            Logger.getLogger(AnalisadorSintatico.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        casaToken("ID");
                                        if (casaToken(")")) {
                                            if (casaToken(":")) {
                                                DeclaraVariasID();
                                                ListaCmd();
                                                if (casaToken("end")) {
                                                    if (!casaToken(";")) {
                                                        Error(e1, ";");
                                                    }
                                                } else {
                                                    Error(e1, "end");
                                                }
                                            } else {
                                                Error(e1, ":");
                                            }
                                        } else {
                                            Error(e1, ")");
                                        }
                                    } else {
                                        Error(e1, "ID");
                                    }

                                } else {
                                    Error(e1, "}");
                                }
                            } else {
                                Error(e1, "{");
                            }
                        } else {
                            Error(e1, "String");
                        }
                    } else {
                        Error(e1, "(");
                    }
                } else {
                    Error(e1, "main");
                }
            } else {
                Error(e1, "void");
            }
        } else {
            Error(e1, "defstatic");
        }
    }

    static boolean casaToken(String token) {
        if (!listaTokens.isEmpty()) {
            if (listaTokens.get(0).getTipo().equals(token)) {
                listaTokens.remove(0);
                return true;
            }
            return false;
        }
        return false;
    }

    static boolean isToken(String token) {
        if (!listaTokens.isEmpty()) {
            return listaTokens.get(0).getTipo().equals(token);
        }
        return false;
    }

    static void Error(String erro, String tokenEsperado) {
        result = false;
        contError++;
        switch (erro) {
            case "declaração":
                while (result == false && !listaTokens.isEmpty()) {
                    if (casaToken(";") || casaToken(".") || casaToken(")")) {
                        result = true;
                    } else {
                        listaTokens.remove(0);
                    }
                }
                System.err.println("Variável " + "'" + tokenEsperado + "'" + " não foi declarada!");
                break;
                
            case "duplicidade":
                while (result == false && !listaTokens.isEmpty()) {
                    if (casaToken(";") || casaToken(".") || casaToken(")")) {
                        result = true;
                    } else {
                        listaTokens.remove(0);
                    }
                }
                System.err.println("Variável " + "'" + tokenEsperado + "'" + " declarada em duplicidade!");
                break;
                
            case "token":
                while (result == false && !listaTokens.isEmpty()) {
                    if (casaToken(";") || casaToken(".") || casaToken(")")) {
                        result = true;
                    } else {
                        listaTokens.remove(0);
                    }
                }
                System.err.println("Token " + "'" + tokenEsperado + "'" + " inesperado!");
                break;
                
            default:
                break;
        }
    }

    public static boolean verificaTabela(String id) {
        try {
            tabelaSimbolos.clear();
            tabelaSimbolos.putAll(ts.getTabela());
        } catch (IOException ex) {
            Logger.getLogger(AnalisadorSintatico.class.getName()).log(Level.SEVERE, null, ex);
        }
        return tabelaSimbolos.values().contains(id);
    }
}
