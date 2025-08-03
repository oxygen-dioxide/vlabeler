package com.sdercolin.vlabeler.exception

import com.sdercolin.vlabeler.ui.string.*

class FailedToCreateProjectException(
    cause: Throwable?,
) : LocalizedException(Strings.FailedToCreateProjectException, cause)
