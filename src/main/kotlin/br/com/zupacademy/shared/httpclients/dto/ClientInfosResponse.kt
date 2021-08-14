package br.com.zupacademy.shared.httpclients.dto

data class ClientInfosResponse (val id: String, val nome: String, val cpf: String, val instituicao: InstituicaoResponse)