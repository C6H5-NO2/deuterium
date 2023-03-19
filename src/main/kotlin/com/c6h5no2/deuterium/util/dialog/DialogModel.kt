package com.c6h5no2.deuterium.util.dialog

import java.nio.file.Path


class DialogModel {
    val openResultDeferred = JbDialogState<Path?>()
    val saveResultDeferred = JbDialogState<Path?>()
    val askToSaveResultDeferred = JbDialogState<AlertDialogResult>()
}
