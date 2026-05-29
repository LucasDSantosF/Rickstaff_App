package com.rickstaff.app.data.model;

public class Employee {
    private int id;
    private String nome;
    private String email;
    private String cargo;
    private double salario;
    private boolean ativo;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getCargo() { return cargo; }
    public void setCargo(String cargo) { this.cargo = cargo; }
    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }
    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }
}