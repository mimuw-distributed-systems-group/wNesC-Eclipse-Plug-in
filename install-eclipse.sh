#!/bin/bash

status_success=0
status_error=1

print_usage() {
   printf "USAGE: $0 <installdir> <updatesitedir> <arch>\n\n"
   printf "   <installdir>     Path to directory where Eclipse will be\n"
   printf "                    installed.\n"
   printf "   <updatesitedir>  Path to the directory with the update site\n"
   printf "                    of the wNesC plug-in.\n"
   printf "   <arch>           Target architecture of Eclipse that will\n"
   printf "                    be installed. It should be one of: x86,\n"
   printf "                    x86-64\n"
}

get_absolute_path() {
   cd "$1"
   abs_path=`pwd`
   cd - &> /dev/null
}

parse_parameters() {
   get_absolute_path "$1"
   installdir="$abs_path"
   get_absolute_path "$2"
   updatesitedir="$abs_path"
   targetarch="$3"
}

check_dir() {
   if [ ! -d "$1" ]
   then
      printf "'$1' does not exist or is not a directory.\n"
      exit $status_error
   fi
}

check_parameters() {
   check_dir "$1"
   check_dir "$2"

   if [ ! -f "$2/site.xml" ]
   then
      printf "Update site directory '$2' does not contain file 'site.xml'.\n"
      exit $status_error
   fi

   case "$3" in
      x86)    ;;
      x86-64) ;;
      *) printf "Invalid target Eclipse architecture: '$3'\n"
         exit $status_error
         ;;
   esac
}

check_java() {
   java_exec=`type -p java`

   if [ -z "$java_exec" ]
   then
      printf "Java is not installed.\n"
      exit $status_error
   fi

   java_version_string=`java -version 2>&1 | head -n 1`
   java_version_string=${java_version_string#java version \"}
   java_version_string=${java_version_string%\"}

   # Parse Java version
   oldIfs="$IFS"
   IFS='.'
   read -a java_version <<< "$java_version_string"
   IFS="$oldIfs"

   # Check Java version
   if [ ${#java_version[*]} -lt 3 ]
   then
      printf "Obtained invalid Java version string: '$java_version_string'\n"
      exit $status_error
   elif [ ${java_version[0]} -lt 1 -o \( ${java_version[0]} -eq 1 -a ${java_version[1]} -lt 7 \) ]
   then
      printf "Too old version of Java is installed. At least Java 7 is required.\n"
      exit $status_error
   fi
}

if [ $# -ne 3 ]
then
   print_usage "$@"
   exit $status_error
fi

check_parameters "$@"
check_java
parse_parameters "$@"

# Determine archive to download
case $targetarch in
   x86)    archive=eclipse-cpp-kepler-SR2-linux-gtk.tar.gz ;;
   x86-64) archive=eclipse-cpp-kepler-SR2-linux-gtk-x86_64.tar.gz ;;
   *) printf "internal error: unexpected target architecture '$targetarch'\n"
      exit $status_error
      ;;
esac

# Download Eclipse with CDT
rm -f "$installdir/$archive"
wget -P "$installdir" "http://www.mirrorservice.org/sites/download.eclipse.org/eclipseMirror/technology/epp/downloads/release/kepler/SR2/$archive"

# Unpack Eclipse and remove the archive
tar -xzf "$installdir/$archive" -C "$installdir"
rm "$installdir/$archive"

# Install plug-in
"$installdir/eclipse/eclipse" \
   -application org.eclipse.equinox.p2.director \
   -noSplash \
   -repository "file://$updatesitedir" \
   -installIU Nesc_Plugin

exit $status_success
