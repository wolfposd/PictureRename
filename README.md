# PictureRename
Renames JPG pictures by extracting EXIF-Date



## Building
    mvn package


## Usage
	$ java -jar PictureRename-0.0.1-jar-with-dependencies.jar testfolder false -9
	Performing dry-run, no pictures will be changed
	Adjusting Time by -9 hours
	image1.jpg -> 20170325_082551.jpg
	image1.jpg -> 20170325_082555.jpg
	image1.jpg -> 20170325_082620.jpg
	image1.jpg -> 20170325_091437.jpg
