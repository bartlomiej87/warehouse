package com.challenge.warehouse.error

object ErrorCodes {
   const val WRONG_METRIC_PARAMETER = "Wrong metric parameters. Available values clicks or impressions"
   const val WRONG_DIMENSION_PARAMETER = "Wrong dimension parameters. Available values datasource or campaign"
   const val WRONG_DATE_RANGE = "Date from greater than date to"
   const val WRONG_SORT_BY_PARAMETERS = "Wrong sort by parameters. Available values clicks, impressions or ctr"
}