#until (mc alias set files-minio http://minio:9000 root 12345678); do
#  echo 'Waiting for MinIO to start...';
#  sleep 3;
#done;
## change to env
#mc mb files-minio/websites || true
#mc mb files-minio/logs || true
#mc mb files-minio/backups || true


until (mc alias set myminio http://minio:9000 root 12345678); do
        echo 'Waiting for MinIO to start...';
        sleep 3;
      done;
echo 'Started MinIO'
mc mb myminio/sounds || true;
echo 'Buckets created successfully.'