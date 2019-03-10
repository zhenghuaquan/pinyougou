/** 定义搜索控制器 */
app.controller("searchController" ,function ($scope, $sce, $location,baseService) {

   /** 定义搜索参数对象 */
   $scope.searchParam = {keywords : '',category : '',
       brand : '',price : '',spec : {}, page : 1, rows : 5, sortField : '', sort : ''};
   /** 定义搜索方法 */
   $scope.search = function () {
       baseService.sendPost("/Search",$scope.searchParam).then(function (response) {
           /** 获取搜索结果 */
           $scope.resultMap = response.data;
           // 初始化页码数组
           $scope.initPageNum();
       })
   }

    // 初始化页码数组
    $scope.initPageNum = function () {
        // 定义页码数组
        $scope.pageNums = [];
        // 获取总页数
        var totalPages = $scope.resultMap.totalPages;
        // 开始页码
        var firstPage = 1;
        // 结束页码
        var lastPage = $scope.resultMap.totalPages;

        /** 前面有点 */
        $scope.firstDot = true;
        /** 后面有点 */
        $scope.lastDot = true;

        // 如果总页数大于5，显示部分页码
        if (totalPages > 5) {
            // 如果当前页码处于前面位置
            if ($scope.searchParam.page <= 3) {
                // 生成前5页页码
                lastPage = 5;
                // 前面没点
                $scope.firstDot = false;
            }
            // 如果当前页码处于后面位置
            else if ($scope.searchParam.page >= totalPages - 3) {
                // 生成后5页页码
                firstPage = totalPages - 4;
                // 后面没点
                $scope.lastD = false;
            }else {
                // 当前页码处于中间位置
                firstPage = $scope.searchParam.page - 2;
                lastPage = $scope.searchParam.page + 2;
            }
        }else {
            $scope.firstDot = false;
            $scope.lastDot = false;
        }
        // 循环产生页码
        for (var i = firstPage; i <= lastPage ; i++){
            $scope.pageNums.push(i);
        }
    }

    /** 根据分页搜索方法 */
    $scope.pageSearch = function (page) {
        page = parseInt(page);
        /** 页码验证 */
        if (page >= 1
            && page <= $scope.resultMap.totalPages
            && page != $scope.searchParam.page) {
            $scope.searchParam.page = page;
            $scope.search();
        }
    }

    /** 定义排序搜索方法 */
    $scope.sortSearch = function (sortField,sort) {
        $scope.searchParam.sortField = sortField;
        $scope.searchParam.sort = sort;
        $scope.search();
    }

   $scope.trustHtml = function (html) {
       return $sce.trustAsHtml(html);
   }

   $scope.addSearchItem = function (key,value) {
        /** 判断是商品分类、品牌、价格 */
        if (key == 'category' || key == 'brand' || key == 'price') {
            $scope.searchParam[key] = value;
        } else {
            /** 规格选项 */
            $scope.searchParam.spec[key] = value;
        }
        /** 执行搜索 */
        $scope.search();
   }

   /** 删除搜索选项的方法 */
   $scope.removeSearchItem = function (key) {
       /** 判断是商品分类、品牌、价格 */
       if (key == 'category' || key == 'brand' || key == 'price') {
           $scope.searchParam[key] = "";
       } else {
           delete $scope.searchParam.spec[key] ;
       }
       /** 执行搜索 */
       $scope.search();
   }

   /** 获取检索关键字 */
   $scope.getkeywords = function () {
       $scope.searchParam.keywords = $location.search().keywords;
       console.log("searchController_keywords=" + $scope.searchParam.keywords);
       $scope.search();
   }
});
