// 控制文件分片和上传
// 不要忘记控制前端的显示结果
// 简单尝试直接使用串行

const API = {
    UPLOAD_SEGMENT_FILE: '/api/upload/segmentFile',
    UPLOAD_CHECK_FILE: '/api/upload/checkFile'
}

/**
 * 文件key计算
 * @param file
 * @returns {string}
 */
function getFileKey(file) {
    //把文件的信息存储为一个字符串
    const fileDetails = file.name + file.size + file.type + file['lastModifiedDate'];
    //使用当前文件的信息用md5加密生成一个key
    const key = hex_md5(fileDetails);
    const key10 = parseInt(key, 16);
    //把加密的信息 转为一个62位的
    return Tool._10to62(key10);
}

function calculateMD5(file) {
    let blobSlice = File.prototype['mozSlice'] || File.prototype['webkitSlice'] || File.prototype.slice;
    let chunkSize = 2097152;
    // read in chunks of 2MB
    let chunks = Math.ceil(file.size / chunkSize);
    let currentChunk = 0;
    let spark = new SparkMD5();
    while (currentChunk++ < chunks) {
        let start = currentChunk * chunkSize;
        let end = start + chunkSize >= file.size ? file.size : start + chunkSize;
        let blob = blobSlice.call(file, start, end);
        spark.appendBinary(blob);
    }
    return spark.end();
}

// 得到分片数量
// 注意分片序号从1开始
function getTotalSegmentCount(file, segmentSize) {
    const size = file.size;
    return Math.ceil(size / segmentSize);
}

// 计算分片的开始和结束
function getSegmentStartAndEnd(file, segmentIndex, segmentSize) {
    const start = (segmentIndex - 1) * segmentSize;
    const end = Math.min(start + segmentSize, file.size);
    return [start, end];
}

// 检测当前文件是否存在，存在且完成上传则输出秒传信息
// 存在但未完成，则将upload的segmentIndex修改，等待后续上传（把前端信息也修改一下）
// 不存在则(key)，等待后续上传（把前端信息也修改一下）
async function checkFile(file) {
    const key = getFileKey(file)
    // axios请求找下数据库中该文件是否存在
    const res = await axios(API.UPLOAD_CHECK_FILE, {
            method: 'POST',
            data: {
                'key': key
            }
        }
    ).catch(err => {
        throw new Error("check请求错误" + err.data)
    });
    return res.data.data;
}

// 总的上传方法，中间循环上传分片
async function uploadFile(file, callback) {
    // 计算MD5为了实现秒传功能
    const md5 = calculateMD5(file);

    const key = getFileKey(file)

    // axios请求找下数据库中该文件是否存在
    let data = await checkFile(file);
    // 分片大小
    const segmentSize = 2 * 1024 * 1024;  // 先2MB用着
    // 默认分片索引为1
    let segmentIndex = 0;
    // 获取文件总分片数
    const segmentTotal = getTotalSegmentCount(file, segmentSize);

    // 如果存在，获取上传状态
    if (data) {
        segmentIndex = data['segmentIndex'];
    }

    // 如果上传已完成，直接返回
    if (segmentIndex === segmentTotal) {
        callback(null, data);
        return;
    }

    // 如果上传未完成，继续上传
    let index = segmentIndex;
    // 断点续传索引处理
    if (index !== 0) {
        index += 1;
    }
    while (index <= segmentTotal) {
        const data = await uploadSegmentFile(file, file.name, file.size, index, segmentTotal, segmentSize, key, md5);
        index = data['segmentIndex'] + 1;
        callback(null, data);
        if (!data) {
            return;
        }
    }

}


// 上传分片
async function uploadSegmentFile(file, fileName, fileSize, segmentIndex, segmentTotal, segmentSize, key, md5) {
    const formData = new FormData();
    const sAe = getSegmentStartAndEnd(file, segmentIndex, segmentSize);
    const segmentFile = file.slice(sAe[0], sAe[1]);

    formData.append('fileName', fileName)
    formData.append('fileSize', fileSize)
    formData.append('segmentFile', segmentFile)
    formData.append('segmentIndex', segmentIndex)
    formData.append('segmentSize', segmentSize)
    formData.append('segmentTotal', segmentTotal)
    formData.append('key', key)
    formData.append('MD5', md5)

    const res = await axios(API.UPLOAD_SEGMENT_FILE, {
        method: 'POST',
        data: formData,
    }).catch(() => {
        throw new Error("分片" + segmentIndex + "上传失败")
    });
    return res.data.data;
}