package com.infobox.hasnat.ume.ume.Model

class Requests {

    var user_name: String? = null
    var user_status: String? = null
    var user_thumb_image: String? = null

    constructor() {}

    constructor(user_name: String, user_status: String, user_thumb_image: String) {
        this.user_name = user_name
        this.user_status = user_status
        this.user_thumb_image = user_thumb_image
    }
}
