// 控制文件分片和上传
// 不要忘记控制前端的显示结果
// 简单尝试直接使用串行

let segmentIndex = 0;
const segmentSize = 2 * 1024 * 1024;  // 先2MB用着


const API = {
    UPLOAD_SEGMENT_FILE: 'http://localhost:8080/api/upload/segmentFile',
    UPLOAD_CHECK_FILE: 'http://localhost:8080/api/upload/checkFile'
}

async function checkFile_() {
    const file = $('#file').get(0).files[0];
    await checkFile(file);
}

async function uploadFile_() {
    const file = $('#file').get(0).files[0];
    await uploadFile(file);
}

// 文件key计算
function getFileKey(file) {
    console.log(file);
    //把文件的信息存储为一个字符串
    const fileDetails = file.name + file.size + file.type + file['lastModifiedDate'];
    console.log(fileDetails)
    //使用当前文件的信息用md5加密生成一个key
    const key = hex_md5(fileDetails);
    const key10 = parseInt(key, 16);
    //把加密的信息 转为一个62位的
    const key62=Tool._10to62(key10);
    // console.log("getFileKey:" + key62)
    return key62;
}

// 得到分片数量
// 注意分片序号从1开始
function getTotalSegmentCount(file, segmentSize) {
    const size = file.size;
    return Math.ceil(size / segmentSize);
}

// 计算分片的开始和结束
function getSegmentStartAndEnd(file, segmentIndex) {
    const start = (segmentIndex - 1) * segmentSize;
    const end = Math.min(start + segmentSize, file.size);
    return [start, end];
}

// 检测当前文件是否存在，存在且完成上传则输出秒传信息
// 存在但未完成，则将upload的segmentIndex修改，等待后续上传（把前端信息也修改一下）
// 不存在则md5码(key)，等待后续上传（把前端信息也修改一下）
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
        $('#output').html("check请求错误")
        console.error("check请求错误" + err)
    });
    const data = res.data.data;

    if (!data) {
        $('#fileName').html('文件名:')
        $('#output').html('该文件未上传')
        return data;
    }

    const name = data['name'];
    const fileUrl = data['url']
    const segmentIndexNow = data['segmentIndex'];
    const segmentTotal = data['segmentTotal'];
    $('#fileName').html('文件名: ' + name)
    if (segmentIndexNow === segmentTotal) {
        // 完成上传
        $('#output').html('该文件已上传')
        $('#fileUrl').html('文件链接: ' + fileUrl)
        return data;
    }
    $('#output').html(segmentIndexNow + '/' + segmentTotal)
    segmentIndex = segmentIndexNow + 1
    return data;
}

// 总的上传方法，中间递归上传分片
async function uploadFile(file) {
    const key = getFileKey(file)
    // axios请求找下数据库中该文件是否存在
    const data = await checkFile(file);
    let segmentIndex = 0;
    let segmentTotal = getTotalSegmentCount(file, segmentSize);

    // 如果文件不存在 从头开始上传
    if (!data) {
        $('#output').html(segmentIndex + '/' + segmentTotal)
        segmentIndex = segmentIndex + 1;
        // 开始上传分片
        await uploadSegmentFile(file, segmentIndex, key)
        return
    }

    // 如果存在
    const fileName = data['name'];
    $('#fileName').html('文件名: ' + fileName)
    const fileUrl = data['url']
    segmentIndex = data['segmentIndex'];
    segmentTotal = data['segmentTotal'];

    // 上传已完成
    if (segmentIndex === segmentTotal) {
        // 完成上传
        $('#output').html('该文件已上传')
        $('#fileUrl').html('文件链接: ' + fileUrl)
        console.log(data)
        return;
    }

    // 上传未完成 继续上传
    $('#output').html(segmentIndex + '/' + segmentTotal)
    segmentIndex = segmentIndex + 1;
    // 开始上传分片
    await uploadSegmentFile(file, segmentIndex, key)
}


// 上传分片
async function uploadSegmentFile(file, segmentIndex, key) {
    const formData = new FormData();
    const sAe = getSegmentStartAndEnd(file, segmentIndex);
    const segmentTotal = getTotalSegmentCount(file, segmentSize);

    formData.append('segmentFile', file.slice(sAe[0], sAe[1]))
    formData.append('fileName', file.name)
    formData.append('fileSize', file.size)
    formData.append('segmentIndex', segmentIndex)
    formData.append('segmentSize', segmentSize.toString())
    formData.append('key', key)

    const res = await axios(API.UPLOAD_SEGMENT_FILE, {
        method: 'POST',
        data: formData,
    }).catch(() => {
        console.error("分片" + segmentIndex + "上传失败")
    });
    const data = res.data.data;
    if (!data) {
        $('#output').html(data)
        return
    }
    const fileName = data['name'];
    const fileUrl = data['url']
    $('#fileName').html('文件名: ' + fileName)
    // 递归调用
    $('#output').html(segmentIndex + "/" + segmentTotal)
    if (segmentIndex < segmentTotal)
        await uploadSegmentFile(file, segmentIndex + 1, key)
    else if (segmentIndex === segmentTotal) {
        // 完成上传
        $('#output').html('上传完成')
        $('#fileUrl').html('文件链接: ' + fileUrl)
        console.log(data)
    }

}