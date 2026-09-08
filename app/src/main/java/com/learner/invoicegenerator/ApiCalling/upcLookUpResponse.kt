package com.learner.invoicegenerator.ApiCalling

data class upcLookUpResponse(val items: List<upcItem> )

data class upcItem(val ean:String,val title:String,val lowest_recorded_price:Double )
