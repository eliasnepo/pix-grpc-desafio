package br.com.zupacademy.shared.httpclients.dto

import br.com.zupacademy.AccountType

fun AccountType.convert(): AccountTypeBacen {
    if (this == AccountType.CONTA_CORRENTE) {
        return AccountTypeBacen.CACC
    }
    return AccountTypeBacen.SVGS
}