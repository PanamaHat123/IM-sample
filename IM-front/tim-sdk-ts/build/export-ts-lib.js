const fs = require("fs");
const path = require("path");
const JSZIP = require("jszip");

/**
 *  update script
 * */

// Copy folder
function copyDirectory(src, dest) {
    if (fs.existsSync(dest) === false) {
        fs.mkdirSync(dest);
    }
    if (fs.existsSync(src) === false) {
        return false;
    }
    var dirs = fs.readdirSync(src);
    dirs.forEach(function(item) {
        var item_path = path.join(src, item);
        var temp = fs.statSync(item_path);
        if (temp.isFile()) {
            fs.copyFileSync(item_path, path.join(dest, item));
        } else if (temp.isDirectory()) {
            copyDirectory(item_path, path.join(dest, item));
        }
    });
}


function deleteDirectory(dir) {
    if (fs.existsSync(dir) === true) {
        var files = fs.readdirSync(dir);
        files.forEach(function(item) {
            var item_path = path.join(dir, item);
            // console.log(item_path);
            if (fs.statSync(item_path).isDirectory()) {
                deleteDirectory(item_path);
            } else {
                fs.unlinkSync(item_path);
            }
        });
        fs.rmdirSync(dir);
    }
}


function eachTargetFileInDir(dir, suffix, cb){
    const srcFiles = fs.readdirSync(dir);
    srcFiles.forEach(function(item) {
        var itemPath = path.join(dir, item);
        var itemStat = fs.statSync(itemPath);
        // console.log(`[${itemStat.isFile() ? 'file' : 'dir'}] path: ${itemPath}`);
        if(itemStat.isFile() && itemPath.endsWith(suffix)){
            cb(itemPath);
        }
        else if(itemStat.isDirectory()){
            eachTargetFileInDir(itemPath, suffix, cb);
        }
    });
}

function zipDir(source, target) {
    let _zip = new JSZIP();
    function _compress(_source, ...dirname) {
        fs.readdirSync(_source).map(name => {
            let _path = path.join(_source, name);
            fs.lstatSync(_path).isDirectory()
              ? _compress(_path, ...dirname, name)
              : dirname.reduce((pre, cur) => pre.folder(cur),_zip.folder(path.basename(source))).file(name, fs.createReadStream(_path));
        });
        return _zip;
    }
    return _compress(source)
      .generateNodeStream({ type: "nodebuffer", compression: "DEFLATE" })
      .pipe(fs.createWriteStream(target))
      .on("finish", () => console.log("Zip Packing Successfully! "))
      .on("error", err => console.log("Zip Packing Error: \n"));
}


function main() {
    const TARGET_SOURCE_PATH = 'dist/lim';
    console.log(`===== 1.prepare packaging ====`);
    if (fs.existsSync('dist')) {
        deleteDirectory('dist');
    }
    fs.mkdirSync('dist');
    fs.mkdirSync('dist/lim');
    console.log(`===== 2.copying ====`);
    copyDirectory('src/lim', TARGET_SOURCE_PATH);

    console.log(`===== 3.Handle conditional compilation content on source files(IFTRUE_WXAPP, IFTRUE_WEBAPP) ====`);
    eachTargetFileInDir(TARGET_SOURCE_PATH, '.ts', (name)=>{
        const f = fs.readFileSync(name);
        const fileContent = f.toString();
        const fileContentReplace = fileContent
          .replace(/[ ]+\/\*IFTRUE_WXAPP\*\/((.|\n|\r\n)*)?[ ]+\/\*FITRUE_WXAPP\*\//g, '')
          .replace(/[ ]+\/\*IFTRUE_WEBAPP\*\/((.|\n|\r\n)*)?[ ]+\/\*FITRUE_WEBAPP\*\//g, '');

        fs.writeFileSync(name, fileContentReplace, 'utf8');
    });

    console.log(`===== 4.lim-last.zip ====`);
    zipDir(TARGET_SOURCE_PATH, 'dist/lim-last.zip');

    console.log(`dist/lim-last.zip is ready!`);
}


main();
